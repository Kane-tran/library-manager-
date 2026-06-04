package com.library.service;

import com.library.entity.BorrowRecord;
import com.library.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BorrowService {
    BorrowRecord borrowBook(Long userId, Long bookId);
    BorrowRecord returnBook(Long recordId);
    BorrowRecord findById(Long id);
    Page<BorrowRecord> findAll(Pageable pageable);
    Page<BorrowRecord> findByUser(User user, Pageable pageable);
    Page<BorrowRecord> findByStatus(BorrowRecord.Status status, Pageable pageable);
    List<BorrowRecord> findOverdue();
    void markLost(Long recordId);
}
