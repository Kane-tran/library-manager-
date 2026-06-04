package com.library.repository;

import com.library.entity.Book;
import com.library.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);
    boolean existsByIsbn(String isbn);

    @Query("SELECT b FROM Book b LEFT JOIN b.authors a WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(a.name) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Book> searchBooks(@Param("q") String q, Pageable pageable);

    @Query("SELECT b FROM Book b LEFT JOIN b.authors a WHERE " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(b.isbn) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(a.name) LIKE LOWER(CONCAT('%', :q, '%'))) AND " +
           "b.category = :category")
    Page<Book> searchBooksInCategory(@Param("q") String q, @Param("category") Category category, Pageable pageable);

    Page<Book> findByCategory(Category category, Pageable pageable);
    Page<Book> findByAvailableCopiesGreaterThan(int copies, Pageable pageable);

    long countByAvailableCopiesGreaterThan(int copies);
}
