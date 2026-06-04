package com.library.controller;

import com.library.entity.Reservation;
import com.library.entity.User;
import com.library.service.ReservationService;
import com.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final UserService userService;

    @GetMapping
    public String myReservations(@RequestParam(defaultValue = "0") int page,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        Page<Reservation> reservations = reservationService.findByUser(user,
            PageRequest.of(page, 10, Sort.by("createdAt").descending()));
        model.addAttribute("reservations", reservations);
        model.addAttribute("currentUser", user);
        return "reservation/my-reservations";
    }

    @PostMapping("/reserve/{bookId}")
    public String reserve(@PathVariable Long bookId,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername());
        try {
            reservationService.reserve(user.getId(), bookId);
            redirectAttributes.addFlashAttribute("success", "Reservation placed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/books/" + bookId;
    }

    @PostMapping("/{id}/cancel")
    public String cancelReservation(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername());
        try {
            reservationService.cancelReservation(id, user);
            redirectAttributes.addFlashAttribute("success", "Reservation cancelled");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reservations";
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('LIBRARIAN','MANAGER')")
    public String allReservations(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Reservation> reservations = reservationService.findAll(
            PageRequest.of(page, 15, Sort.by("createdAt").descending()));
        model.addAttribute("reservations", reservations);
        return "reservation/all-reservations";
    }
}
