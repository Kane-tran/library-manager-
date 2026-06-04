package com.library.repository;

import com.library.entity.Book;
import com.library.entity.Review;
import com.library.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByBookOrderByCreatedAtDesc(Book book);

    Optional<Review> findByUserAndBook(User user, Book book);

    boolean existsByUserAndBook(User user, Book book);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.book = :book")
    Double getAverageRating(@Param("book") Book book);

    long countByBook(Book book);
}
