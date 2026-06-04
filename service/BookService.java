package com.library.service;

import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookService {
    Book save(Book book, List<Long> authorIds);
    Book findById(Long id);
    void delete(Long id);
    Page<Book> findAll(Pageable pageable);
    Page<Book> search(String q, Long categoryId, Pageable pageable);
    List<Category> findAllCategories();
    Category saveCategory(Category category);
    Category findCategoryById(Long id);
    void deleteCategory(Long id);
    List<Author> findAllAuthors();
    Author saveAuthor(Author author);
    Author findAuthorById(Long id);
    void deleteAuthor(Long id);
    int importFromCsv(MultipartFile file);
}
