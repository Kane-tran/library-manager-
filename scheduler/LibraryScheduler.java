package com.library.scheduler;

import com.library.entity.BorrowRecord;
import com.library.entity.Reservation;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.ReservationRepository;
import com.library.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LibraryScheduler {

    private final BorrowRecordRepository borrowRecordRepository;
    private final ReservationRepository reservationRepository;
    private final EmailService emailService;

    // Every day at 8 AM: mark overdue, send reminders
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void checkOverdueAndSendReminders() {
        log.info("Running daily overdue check...");
        LocalDate today = LocalDate.now();

        // Mark active records past due date as OVERDUE
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findOverdueRecords(today);
        for (BorrowRecord record : overdueRecords) {
            record.setStatus(BorrowRecord.Status.OVERDUE);
            borrowRecordRepository.save(record);
            emailService.sendOverdueNotice(record);
        }
        log.info("Marked {} records as overdue", overdueRecords.size());

        // Send due-soon reminders (due in next 3 days)
        LocalDate soonDate = today.plusDays(3);
        List<BorrowRecord> dueSoon = borrowRecordRepository.findDueSoonRecords(today, soonDate);
        for (BorrowRecord record : dueSoon) {
            emailService.sendDueSoonReminder(record);
        }
        log.info("Sent {} due-soon reminders", dueSoon.size());

        // Expire READY reservations past expiry date
        List<Reservation> expired = reservationRepository.findExpiredReservations(today);
        for (Reservation r : expired) {
            r.setStatus(Reservation.Status.EXPIRED);
            reservationRepository.save(r);
        }
        log.info("Expired {} reservations", expired.size());
    }
}
