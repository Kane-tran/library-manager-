package com.library.controller;

import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.service.BorrowService;
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
@RequestMapping("/borrow")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;
    private final UserService userService;

    @GetMapping
    public String myBorrows(@RequestParam(defaultValue = "0") int page,
                            @AuthenticationPrincipal UserDetails userDetails,
                            Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        Page<BorrowRecord> records = borrowService.findByUser(user, PageRequest.of(page, 10, Sort.by("borrowDate").descending()));
        model.addAttribute("records", records);
        model.addAttribute("currentUser", user);
        return "borrow/my-borrows";
    }

    @PostMapping("/borrow/{bookId}")
    public String borrowBook(@PathVariable Long bookId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername());
        try {
            borrowService.borrowBook(user.getId(), bookId);
            redirectAttributes.addFlashAttribute("success", "Book borrowed successfully! Due date: " +
                borrowService.findAll(PageRequest.of(0, 1)).getContent().get(0).getDueDate());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/books/" + bookId;
    }

    // Librarian/Manager only: all borrow records
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('LIBRARIAN','MANAGER')")
    public String allBorrows(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(required = false) String status,
                             Model model) {
        Page<BorrowRecord> records;
        if (status != null && !status.isBlank()) {
            records = borrowService.findByStatus(BorrowRecord.Status.valueOf(status),
                PageRequest.of(page, 15, Sort.by("borrowDate").descending()));
        } else {
            records = borrowService.findAll(PageRequest.of(page, 15, Sort.by("borrowDate").descending()));
        }
        model.addAttribute("records", records);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", BorrowRecord.Status.values());
        return "borrow/all-borrows";
    }

    // Librarian/Manager return từ trang all-borrows
    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('LIBRARIAN','MANAGER')")
    public String returnBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            borrowService.returnBook(id);
            redirectAttributes.addFlashAttribute("success", "Book returned successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/borrow/all";
    }

    // User tự trả sách của mình
    @PostMapping("/{id}/return-self")
    public String returnSelf(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername());
        try {
            BorrowRecord record = borrowService.findById(id);
            // Chỉ cho trả chính sách của mình
            if (!record.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "You can only return your own books");
                return "redirect:/borrow";
            }
            if (record.getStatus() == BorrowRecord.Status.RETURNED) {
                redirectAttributes.addFlashAttribute("error", "Book already returned");
                return "redirect:/borrow";
            }
            borrowService.returnBook(id);
            redirectAttributes.addFlashAttribute("success",
                "Book \"" + record.getBook().getTitle() + "\" returned successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/borrow";
    }

    @PostMapping("/{id}/lost")
    @PreAuthorize("hasAnyRole('LIBRARIAN','MANAGER')")
    public String markLost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        borrowService.markLost(id);
        redirectAttributes.addFlashAttribute("success", "Book marked as lost");
        return "redirect:/borrow/all";
    }

    // Librarian borrows book for a member
    @GetMapping("/issue")
    @PreAuthorize("hasAnyRole('LIBRARIAN','MANAGER')")
    public String issueForm(@RequestParam(required = false) Long bookId, Model model) {
        model.addAttribute("bookId", bookId);
        return "borrow/issue";
    }

    @PostMapping("/issue")
    @PreAuthorize("hasAnyRole('LIBRARIAN','MANAGER')")
    public String issueBook(@RequestParam Long userId,
                            @RequestParam Long bookId,
                            RedirectAttributes redirectAttributes) {
        try {
            borrowService.borrowBook(userId, bookId);
            redirectAttributes.addFlashAttribute("success", "Book issued successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/borrow/all";
    }
}
