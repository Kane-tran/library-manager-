package com.library.controller;

import com.library.entity.User;
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
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Profile
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("user", userService.findByUsername(userDetails.getUsername()));
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @ModelAttribute User userForm,
                                RedirectAttributes redirectAttributes) {
        User current = userService.findByUsername(userDetails.getUsername());
        userForm.setId(current.getId());
        userForm.setRole(current.getRole()); // cannot change own role
        userService.update(userForm);
        redirectAttributes.addFlashAttribute("success", "Profile updated");
        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match");
            return "redirect:/profile";
        }
        User user = userService.findByUsername(userDetails.getUsername());
        try {
            userService.changePassword(user.getId(), oldPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "Password changed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    // Admin: user management
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('MANAGER')")
    public String listUsers(@RequestParam(defaultValue = "") String q,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {
        Page<User> users = userService.search(q, PageRequest.of(page, 15, Sort.by("fullName")));
        model.addAttribute("users", users);
        model.addAttribute("q", q);
        return "admin/users";
    }

    @GetMapping("/admin/users/{id}/edit")
    @PreAuthorize("hasRole('MANAGER')")
    public String editUserForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.findById(id));
        model.addAttribute("roles", User.Role.values());
        return "admin/user-edit";
    }

    @PostMapping("/admin/users/{id}/update")
    @PreAuthorize("hasRole('MANAGER')")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute User userForm,
                             RedirectAttributes redirectAttributes) {
        userForm.setId(id);
        userService.update(userForm);
        redirectAttributes.addFlashAttribute("success", "User updated");
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/toggle")
    @PreAuthorize("hasRole('MANAGER')")
    public String toggleUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.toggleEnabled(id);
        redirectAttributes.addFlashAttribute("success", "User status updated");
        return "redirect:/admin/users";
    }
}
