package com.library.repository;

import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    Page<BorrowRecord> findByUser(User user, Pageable pageable);
    Page<BorrowRecord> findByStatus(BorrowRecord.Status status, Pageable pageable);
    List<BorrowRecord> findByUserAndStatus(User user, BorrowRecord.Status status);

    Optional<BorrowRecord> findByUserAndBookAndStatus(User user, Book book, BorrowRecord.Status status);

    long countByUserAndStatus(User user, BorrowRecord.Status status);

    // Overdue: active records past due date
    @Query("SELECT b FROM BorrowRecord b WHERE b.status = 'ACTIVE' AND b.dueDate < :today")
    List<BorrowRecord> findOverdueRecords(@Param("today") LocalDate today);

    // Due soon (within next 3 days) for reminders
    @Query("SELECT b FROM BorrowRecord b WHERE b.status = 'ACTIVE' AND b.dueDate BETWEEN :today AND :soon")
    List<BorrowRecord> findDueSoonRecords(@Param("today") LocalDate today, @Param("soon") LocalDate soon);

    long countByStatus(BorrowRecord.Status status);
}
