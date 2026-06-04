package com.library.controller;

import com.library.entity.User;
import com.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                           @RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam(required = false, defaultValue = "") String studentId,
                           @RequestParam(required = false, defaultValue = "") String phone,
                           @RequestParam(defaultValue = "STUDENT") String role,
                           RedirectAttributes redirectAttributes,
                           Model model) {

        // Validate username
        if (username == null || username.trim().length() < 3) {
            model.addAttribute("usernameError", "Username must be at least 3 characters");
            return "auth/register";
        }
        if (userService.usernameExists(username.trim())) {
            model.addAttribute("usernameError", "Username already taken");
            return "auth/register";
        }

        // Validate email
        if (userService.emailExists(email.trim())) {
            model.addAttribute("emailError", "Email already registered");
            return "auth/register";
        }

        // Validate password
        if (password == null || password.length() < 6) {
            model.addAttribute("errorMessage", "Password must be at least 6 characters");
            return "auth/register";
        }

        // Create user
        User user = new User();
        user.setFullName(fullName.trim());
        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setPassword(password);
        user.setRole(User.Role.valueOf(role));
        if (!studentId.isBlank()) user.setStudentId(studentId.trim());
        if (!phone.isBlank()) user.setPhone(phone.trim());

        userService.register(user);

        redirectAttributes.addFlashAttribute("success",
            "Registration successful! Please log in with your new account.");
        return "redirect:/auth/login";
    }
}
