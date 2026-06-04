package com.library.controller;

import com.library.entity.Fine;
import com.library.entity.User;
import com.library.service.FineServiceImpl;
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

import java.math.BigDecimal;

@Controller
@RequestMapping("/fines")
@RequiredArgsConstructor
public class FineController {

    private final FineServiceImpl fineService;
    private final UserService userService;

    @GetMapping
    public String myFines(@RequestParam(defaultValue = "0") int page,
                          @AuthenticationPrincipal UserDetails userDetails,
                          Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        Page<Fine> fines = fineService.findByUser(user, PageRequest.of(page, 10, Sort.by("createdAt").descending()));
        model.addAttribute("fines", fines);
        model.addAttribute("totalUnpaid", fineService.getTotalUnpaid(user));
        model.addAttribute("currentUser", user);
        return "fine/my-fines";
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('LIBRARIAN','MANAGER')")
    public String allFines(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Fine> fines = fineService.findAll(PageRequest.of(page, 15, Sort.by("createdAt").descending()));
        model.addAttribute("fines", fines);
        return "fine/all-fines";
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('LIBRARIAN','MANAGER')")
    public String payFine(@PathVariable Long id,
                          @RequestParam BigDecimal amount,
                          RedirectAttributes redirectAttributes) {
        try {
            fineService.payFine(id, amount);
            redirectAttributes.addFlashAttribute("success", "Payment recorded successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/fines/all";
    }

    @PostMapping("/{id}/waive")
    @PreAuthorize("hasRole('MANAGER')")
    public String waiveFine(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        fineService.waiveFine(id);
        redirectAttributes.addFlashAttribute("success", "Fine waived");
        return "redirect:/fines/all";
    }
}
