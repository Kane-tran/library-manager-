package com.library.service;

import com.library.entity.*;
import com.library.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final FineRepository fineRepository;
    private final ReservationRepository reservationRepository;
    private final EmailService emailService;

    @Value("${library.fine.per-day:2000}")
    private int finePerDay;

    @Override
    public BorrowRecord borrowBook(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new IllegalArgumentException("Book not found"));

        // Check book availability
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No copies available for: " + book.getTitle());
        }

        // Check borrowing limit
        long activeBorrows = borrowRecordRepository.countByUserAndStatus(user, BorrowRecord.Status.ACTIVE);
        if (activeBorrows >= user.getMaxBooks()) {
            throw new IllegalStateException("Borrow limit reached (" + user.getMaxBooks() + " books max)");
        }

        // Check for unpaid fines
        BigDecimal unpaid = fineRepository.getTotalUnpaidByUser(user);
        if (unpaid != null && unpaid.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Outstanding fines must be paid before borrowing");
        }

        // Check if already borrowed
        boolean alreadyBorrowed = borrowRecordRepository
            .findByUserAndBookAndStatus(user, book, BorrowRecord.Status.ACTIVE).isPresent();
        if (alreadyBorrowed) {
            throw new IllegalStateException("You already have this book borrowed");
        }

        // Create borrow record
        BorrowRecord record = new BorrowRecord();
        record.setUser(user);
        record.setBook(book);
        record.setBorrowDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(user.getLoanDays()));
        record.setStatus(BorrowRecord.Status.ACTIVE);

        // Decrease available copies
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        // Fulfill reservation if exists
        reservationRepository.findByUserAndBookAndStatus(user, book, Reservation.Status.READY)
            .ifPresent(r -> {
                r.setStatus(Reservation.Status.FULFILLED);
                reservationRepository.save(r);
            });

        return borrowRecordRepository.save(record);
    }

    @Override
    public BorrowRecord returnBook(Long recordId) {
        BorrowRecord record = findById(recordId);
        if (record.getStatus() == BorrowRecord.Status.RETURNED) {
            throw new IllegalStateException("Book already returned");
        }

        record.setReturnDate(LocalDate.now());

        // Check if overdue → create fine
        if (record.isOverdue() || (record.getStatus() == BorrowRecord.Status.OVERDUE)) {
            long overdueDays = java.time.temporal.ChronoUnit.DAYS.between(record.getDueDate(), LocalDate.now());
            if (overdueDays > 0) {
                Fine fine = new Fine();
                fine.setBorrowRecord(record);
                fine.setAmount(BigDecimal.valueOf(overdueDays * finePerDay));
                fine.setDescription("Overdue " + overdueDays + " day(s) × " + finePerDay + " VND/day");
                fineRepository.save(fine);
            }
        }

        record.setStatus(BorrowRecord.Status.RETURNED);
        BorrowRecord saved = borrowRecordRepository.save(record);

        // Return copy to book
        Book book = record.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        // Notify next in reservation queue
        List<Reservation> queue = reservationRepository
            .findByBookAndStatusOrderByQueuePosition(book, Reservation.Status.PENDING);
        if (!queue.isEmpty()) {
            Reservation next = queue.get(0);
            next.setStatus(Reservation.Status.READY);
            next.setExpiryDate(LocalDate.now().plusDays(3));
            reservationRepository.save(next);
            emailService.sendReservationReadyNotification(next);
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public BorrowRecord findById(Long id) {
        return borrowRecordRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Borrow record not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BorrowRecord> findAll(Pageable pageable) {
        return borrowRecordRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BorrowRecord> findByUser(User user, Pageable pageable) {
        return borrowRecordRepository.findByUser(user, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BorrowRecord> findByStatus(BorrowRecord.Status status, Pageable pageable) {
        return borrowRecordRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowRecord> findOverdue() {
        return borrowRecordRepository.findOverdueRecords(LocalDate.now());
    }

    @Override
    public void markLost(Long recordId) {
        BorrowRecord record = findById(recordId);
        record.setStatus(BorrowRecord.Status.LOST);
        borrowRecordRepository.save(record);
    }
}
