package com.library.service;

import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.Reservation;
import com.library.entity.User;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    @Override
    public Reservation reserve(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new IllegalArgumentException("Book not found"));

        // Already has active borrow of this book?
        borrowRecordRepository.findByUserAndBookAndStatus(user, book, BorrowRecord.Status.ACTIVE)
            .ifPresent(r -> { throw new IllegalStateException("You already have this book borrowed"); });

        // Already has pending/ready reservation?
        boolean alreadyReserved = reservationRepository.existsByUserAndBookAndStatusIn(
            user, book, List.of(Reservation.Status.PENDING, Reservation.Status.READY));
        if (alreadyReserved) {
            throw new IllegalStateException("You already have a reservation for this book");
        }

        // Calculate queue position
        long queueSize = reservationRepository
            .findByBookAndStatusOrderByQueuePosition(book, Reservation.Status.PENDING).size();

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setStatus(Reservation.Status.PENDING);
        reservation.setQueuePosition((int) queueSize + 1);

        return reservationRepository.save(reservation);
    }

    @Override
    public void cancelReservation(Long reservationId, User currentUser) {
        Reservation reservation = findById(reservationId);

        // Only owner or librarian/manager can cancel
        boolean isOwner = reservation.getUser().getId().equals(currentUser.getId());
        boolean isStaff = currentUser.getRole() == User.Role.LIBRARIAN ||
                          currentUser.getRole() == User.Role.MANAGER;
        if (!isOwner && !isStaff) {
            throw new IllegalStateException("Not authorized to cancel this reservation");
        }

        reservation.setStatus(Reservation.Status.CANCELLED);
        reservationRepository.save(reservation);

        // Re-number queue
        List<Reservation> queue = reservationRepository
            .findByBookAndStatusOrderByQueuePosition(reservation.getBook(), Reservation.Status.PENDING);
        int pos = 1;
        for (Reservation r : queue) {
            r.setQueuePosition(pos++);
            reservationRepository.save(r);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Reservation> findAll(Pageable pageable) {
        return reservationRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Reservation> findByUser(User user, Pageable pageable) {
        return reservationRepository.findByUser(user, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + id));
    }
}
