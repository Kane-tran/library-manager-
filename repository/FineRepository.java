package com.library.repository;

import com.library.entity.Fine;
import com.library.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface FineRepository extends JpaRepository<Fine, Long> {

    @Query("SELECT f FROM Fine f WHERE f.borrowRecord.user = :user")
    Page<Fine> findByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT f FROM Fine f WHERE f.borrowRecord.user = :user AND f.status != 'PAID' AND f.status != 'WAIVED'")
    List<Fine> findUnpaidByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(f.amount - f.paidAmount), 0) FROM Fine f WHERE f.borrowRecord.user = :user AND f.status != 'PAID' AND f.status != 'WAIVED'")
    BigDecimal getTotalUnpaidByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f")
    BigDecimal getTotalFines();

    @Query("SELECT COALESCE(SUM(f.paidAmount), 0) FROM Fine f")
    BigDecimal getTotalCollected();

    long countByStatus(Fine.Status status);
}
