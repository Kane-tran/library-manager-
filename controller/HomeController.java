package com.library.controller;

import com.library.entity.BorrowRecord;
import com.library.entity.Reservation;
import com.library.entity.User;
import com.library.repository.*;
import com.library.service.FineServiceImpl;
import com.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;
    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final ReservationRepository reservationRepository;
    private final FineRepository fineRepository;
    private final FineServiceImpl fineService;

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", user);

        boolean isStaff = user.getRole() == User.Role.LIBRARIAN
                       || user.getRole() == User.Role.MANAGER;

        if (isStaff) {
            // ── Staff / Admin dashboard ──────────────────
            model.addAttribute("totalBooks",
                bookRepository.count());
            model.addAttribute("availableBooks",
                bookRepository.countByAvailableCopiesGreaterThan(0));
            model.addAttribute("activeBorrows",
                borrowRecordRepository.countByStatus(BorrowRecord.Status.ACTIVE));
            model.addAttribute("overdueCount",
                borrowRecordRepository.countByStatus(BorrowRecord.Status.OVERDUE));
            model.addAttribute("pendingReservations",
                reservationRepository.countByStatus(Reservation.Status.PENDING));

            // Đếm users (dùng count() thay vì load toàn bộ)
            model.addAttribute("totalUsers",
                userService.findAll(Pageable.ofSize(1)).getTotalElements());

            BigDecimal totalFines   = fineRepository.getTotalFines();
            BigDecimal collectedFines = fineRepository.getTotalCollected();
            model.addAttribute("totalFines",
                totalFines    != null ? totalFines    : BigDecimal.ZERO);
            model.addAttribute("collectedFines",
                collectedFines != null ? collectedFines : BigDecimal.ZERO);

        } else {
            // ── Member dashboard (Student / Lecturer) ────
            long myActive  = borrowRecordRepository.countByUserAndStatus(user, BorrowRecord.Status.ACTIVE);
            long myOverdue = borrowRecordRepository.countByUserAndStatus(user, BorrowRecord.Status.OVERDUE);

            // ✅ Đếm reservations đang PENDING hoặc READY của user
            long myReservations = reservationRepository
                .findByUser(user, Pageable.ofSize(200))
                .stream()
                .filter(r -> r.getStatus() == Reservation.Status.PENDING
                          || r.getStatus() == Reservation.Status.READY)
                .count();

            BigDecimal myFines = fineService.getTotalUnpaid(user);

            model.addAttribute("myActiveBorrows",  myActive);
            model.addAttribute("myOverdue",        myOverdue);
            model.addAttribute("myReservations",   myReservations);
            model.addAttribute("myUnpaidFines",    myFines);
            model.addAttribute("maxBooks",         user.getMaxBooks());
            model.addAttribute("loanDays",         user.getLoanDays());
        }

        model.addAttribute("isStaff", isStaff);
        return "dashboard";
    }

    @GetMapping("/error/403")
    public String accessDenied() {
        return "error/403";
    }
}
