package com.library.controller;

import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/reports")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
public class ReportController {

    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final UserRepository userRepository;
    private final FineRepository fineRepository;
    private final ReservationRepository reservationRepository;

    @GetMapping
    public String reports(Model model) {
        // Summary stats
        model.addAttribute("totalBooks", bookRepository.count());
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("activeBorrows", borrowRecordRepository.countByStatus(BorrowRecord.Status.ACTIVE));
        model.addAttribute("overdueCount", borrowRecordRepository.countByStatus(BorrowRecord.Status.OVERDUE));
        model.addAttribute("pendingFines", fineRepository.countByStatus(com.library.entity.Fine.Status.PENDING));

        BigDecimal totalFines = fineRepository.getTotalFines();
        BigDecimal collected = fineRepository.getTotalCollected();
        model.addAttribute("totalFines", totalFines != null ? totalFines : BigDecimal.ZERO);
        model.addAttribute("collectedFines", collected != null ? collected : BigDecimal.ZERO);

        // Users by role for chart
        Map<String, Long> usersByRole = new LinkedHashMap<>();
        for (User.Role role : User.Role.values()) {
            usersByRole.put(role.name(), userRepository.countByRole(role));
        }
        model.addAttribute("usersByRole", usersByRole);

        return "admin/reports";
    }
}
