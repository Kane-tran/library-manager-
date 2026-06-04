package com.library.service;

import com.library.entity.Reservation;
import com.library.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservationService {
    Reservation reserve(Long userId, Long bookId);
    void cancelReservation(Long reservationId, User currentUser);
    Page<Reservation> findAll(Pageable pageable);
    Page<Reservation> findByUser(User user, Pageable pageable);
    Reservation findById(Long id);
}
