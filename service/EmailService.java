package com.library.service;

import com.library.entity.BorrowRecord;
import com.library.entity.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendDueSoonReminder(BorrowRecord record) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(record.getUser().getEmail());
            msg.setSubject("[Library] Book Due Soon: " + record.getBook().getTitle());
            msg.setText(String.format(
                "Dear %s,\n\nYour borrowed book '%s' is due on %s.\n" +
                "Please return it on time to avoid fines.\n\nLibrary Management System",
                record.getUser().getFullName(),
                record.getBook().getTitle(),
                record.getDueDate()
            ));
            mailSender.send(msg);
            log.info("Due-soon reminder sent to {}", record.getUser().getEmail());
        } catch (Exception e) {
            log.warn("Failed to send due-soon email: {}", e.getMessage());
        }
    }

    @Async
    public void sendOverdueNotice(BorrowRecord record) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(record.getUser().getEmail());
            msg.setSubject("[Library] Overdue Notice: " + record.getBook().getTitle());
            msg.setText(String.format(
                "Dear %s,\n\nYour borrowed book '%s' was due on %s and is now %d day(s) overdue.\n" +
                "A fine of %d VND/day is accumulating. Please return the book immediately.\n\nLibrary Management System",
                record.getUser().getFullName(),
                record.getBook().getTitle(),
                record.getDueDate(),
                record.getOverdueDays(),
                2000
            ));
            mailSender.send(msg);
            log.info("Overdue notice sent to {}", record.getUser().getEmail());
        } catch (Exception e) {
            log.warn("Failed to send overdue email: {}", e.getMessage());
        }
    }

    @Async
    public void sendReservationReadyNotification(Reservation reservation) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(reservation.getUser().getEmail());
            msg.setSubject("[Library] Your Reserved Book is Ready: " + reservation.getBook().getTitle());
            msg.setText(String.format(
                "Dear %s,\n\nGood news! The book '%s' you reserved is now available.\n" +
                "Please collect it before %s, otherwise your reservation will be cancelled.\n\nLibrary Management System",
                reservation.getUser().getFullName(),
                reservation.getBook().getTitle(),
                reservation.getExpiryDate()
            ));
            mailSender.send(msg);
            log.info("Reservation ready notification sent to {}", reservation.getUser().getEmail());
        } catch (Exception e) {
            log.warn("Failed to send reservation email: {}", e.getMessage());
        }
    }
}
