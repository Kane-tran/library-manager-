package com.library.controller;

import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.Category;
import com.library.entity.User;
import com.library.service.BookService;
import com.library.service.ImageUploadService;
import com.library.service.ReviewService;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final UserService userService;
    private final ImageUploadService imageUploadService;
    private final ReviewService reviewService;

    @GetMapping
    public String listBooks(@RequestParam(defaultValue = "") String q,
                            @RequestParam(defaultValue = "0") Long categoryId,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "12") int size,
                            @AuthenticationPrincipal UserDetails userDetails,
                            Model model) {
        Page<Book> books = bookService.search(q, categoryId, PageRequest.of(page, size, Sort.by("title")));
        model.addAttribute("books", books);
        model.addAttribute("categories", bookService.findAllCategories());
        model.addAttribute("q", q);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("currentUser", userService.findByUsername(userDetails.getUsername()));
        return "books/list";
    }

    @GetMapping("/{id}")
    public String viewBook(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        com.library.entity.Book book = bookService.findById(id);
        User currentUser = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("book", book);
        model.addAttribute("currentUser", currentUser);
        // Review data
        model.addAttribute("reviews", reviewService.getBookReviews(book));
        model.addAttribute("avgRating", reviewService.getAverageRating(book));
        model.addAttribute("reviewCount", reviewService.getReviewCount(book));
        model.addAttribute("userReview", reviewService.getUserReview(currentUser, book));
        model.addAttribute("hasReviewed", reviewService.hasUserReviewed(currentUser, book));
        return "books/detail";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('LIBRARIAN','MANAGER')")
    public String newBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", bookService.findAllCategories());
        model.addAttribute("authors", bookService.findAllAuthors());
        return "books/form";
    }

    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('LIBRARIAN','MANAGER')")
    public String saveBook(@Valid @ModelAttribute("book") Book book,
                           BindingResult result,
                           @RequestParam(required = false) List<Long> authorIds,
                           @RequestParam(required = false) Long categoryId,
                           @RequestParam(value = "coverImageFile", required = false) MultipartFile coverImageFile,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", bookService.findAllCategories());
            model.addAttribute("authors", bookService.findAllAuthors());
            return "books/form";
        }
        if (categoryId != null) {
            book.setCategory(bookService.findCategoryById(categoryId));
        }
        // Xử lý upload ảnh bìa
        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            try {
                String imageUrl = imageUploadService.uploadCoverImage(coverImageFile);
                book.setCoverImageUrl(imageUrl);
            } catch (Exception e) {
                model.addAttribute("imageError", "Image upload failed: " + e.getMessage());
                model.addAttribute("categories", bookService.findAllCategories());
                model.addAttribute("authors", bookService.findAllAuthors());
                return "books/form";
            }
        }
        bookService.save(book, authorIds);
        redirectAttributes.addFlashAttribute("success", "Book saved successfully");
        return "redirect:/books";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('LIBRARIAN','MANAGER')")
    public String editBookForm(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookService.findById(id));
        model.addAttribute("categories", bookService.findAllCategories());
        model.addAttribute("authors", bookService.findAllAuthors());
        return "books/form";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('LIBRARIAN','MANAGER')")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bookService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Book deleted");
        return "redirect:/books";
    }

    // ---- Category management ----
    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('LIBRARIAN','MANAGER')")
    public String listCategories(Model model) {
        model.addAttribute("categories", bookService.findAllCategories());
        model.addAttribute("category", new Category());
        return "books/categories";
    }

    @PostMapping("/categories/save")
    @PreAuthorize("hasAnyRole('LIBRARIAN','MANAGER')")
    public String saveCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        bookService.saveCategory(category);
        redirectAttributes.addFlashAttribute("success", "Category saved");
        return "redirect:/books/categories";
    }

    @PostMapping("/categories/{id}/delete")
    @PreAuthorize("hasRole('MANAGER')")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bookService.deleteCategory(id);
        redirectAttributes.addFlashAttribute("success", "Category deleted");
        return "redirect:/books/categories";
    }

    // ---- Author management ----
    @GetMapping("/authors")
    @PreAuthorize("hasAnyRole('LIBRARIAN','MANAGER')")
    public String listAuthors(@RequestParam(required = false) Long editId, Model model) {
        model.addAttribute("authors", bookService.findAllAuthors());
        if (editId != null) {
            model.addAttribute("editAuthor", bookService.findAuthorById(editId));
        }
        return "books/authors";
    }

    @PostMapping("/authors/save")
    @PreAuthorize("hasAnyRole('LIBRARIAN','MANAGER')")
    public String saveAuthor(@RequestParam(required = false) Long id,
                             @RequestParam String name,
                             @RequestParam(required = false, defaultValue = "") String nationality,
                             @RequestParam(required = false, defaultValue = "") String biography,
                             RedirectAttributes redirectAttributes) {
        if (name == null || name.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Author name is required");
            return "redirect:/books/authors";
        }
        Author author = (id != null) ? bookService.findAuthorById(id) : new Author();
        author.setName(name.trim());
        author.setNationality(nationality.isBlank() ? null : nationality.trim());
        author.setBiography(biography.isBlank() ? null : biography.trim());
        bookService.saveAuthor(author);
        redirectAttributes.addFlashAttribute("success",
            id != null ? "Author updated successfully!" : "Author added successfully!");
        return "redirect:/books/authors";
    }

    @PostMapping("/authors/{id}/delete")
    @PreAuthorize("hasAnyRole('LIBRARIAN','MANAGER')")
    public String deleteAuthor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Author author = bookService.findAuthorById(id);
            bookService.deleteAuthor(id);
            redirectAttributes.addFlashAttribute("success", "Author \"" + author.getName() + "\" deleted");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete author: " + e.getMessage());
        }
        return "redirect:/books/authors";
    }

    // ---- CSV Import ----
    @GetMapping("/import")
    @PreAuthorize("hasAnyRole('LIBRARIAN','MANAGER')")
    public String importPage() {
        return "books/import";
    }

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('LIBRARIAN','MANAGER')")
    public String importCsv(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a CSV file");
            return "redirect:/books/import";
        }
        int count = bookService.importFromCsv(file);
        redirectAttributes.addFlashAttribute("success", "Imported " + count + " books successfully");
        return "redirect:/books";
    }
}
