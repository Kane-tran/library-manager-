package com.library.repository;

import com.library.entity.Book;
import com.library.entity.Reservation;
import com.library.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Page<Reservation> findByUser(User user, Pageable pageable);
    Page<Reservation> findByStatus(Reservation.Status status, Pageable pageable);
    List<Reservation> findByBookAndStatusOrderByQueuePosition(Book book, Reservation.Status status);
    Optional<Reservation> findByUserAndBookAndStatus(User user, Book book, Reservation.Status status);

    boolean existsByUserAndBookAndStatusIn(User user, Book book, List<Reservation.Status> statuses);

    // Find expired READY reservations
    @Query("SELECT r FROM Reservation r WHERE r.status = 'READY' AND r.expiryDate < :today")
    List<Reservation> findExpiredReservations(@Param("today") LocalDate today);

    long countByStatus(Reservation.Status status);
}
