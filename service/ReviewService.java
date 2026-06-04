package com.library.service;

import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.Review;
import com.library.entity.User;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.BookRepository;
import com.library.repository.ReviewRepository;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    public Review addReview(Long userId, Long bookId, int rating, String comment) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new IllegalArgumentException("Book not found"));

        // Chỉ user đã từng mượn sách này mới được đánh giá
        boolean hasBorrowed = !borrowRecordRepository
            .findByUserAndStatus(user, BorrowRecord.Status.RETURNED).isEmpty() ||
            borrowRecordRepository.findByUserAndStatus(user, BorrowRecord.Status.RETURNED)
            .stream().anyMatch(r -> r.getBook().getId().equals(bookId));

        // Kiểm tra đã đánh giá chưa
        if (reviewRepository.existsByUserAndBook(user, book)) {
            throw new IllegalStateException("You have already reviewed this book");
        }

        Review review = new Review();
        review.setUser(user);
        review.setBook(book);
        review.setRating(rating);
        review.setComment(comment.trim());
        return reviewRepository.save(review);
    }

    public Review updateReview(Long reviewId, Long userId, int rating, String comment) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Not authorized to edit this review");
        }
        review.setRating(rating);
        review.setComment(comment.trim());
        return reviewRepository.save(review);
    }

    public void deleteReview(Long reviewId, User currentUser) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        boolean isOwner = review.getUser().getId().equals(currentUser.getId());
        boolean isStaff = currentUser.getRole() == User.Role.LIBRARIAN ||
                          currentUser.getRole() == User.Role.MANAGER;
        if (!isOwner && !isStaff) {
            throw new IllegalStateException("Not authorized to delete this review");
        }
        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public List<Review> getBookReviews(Book book) {
        return reviewRepository.findByBookOrderByCreatedAtDesc(book);
    }

    @Transactional(readOnly = true)
    public double getAverageRating(Book book) {
        Double avg = reviewRepository.getAverageRating(book);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    @Transactional(readOnly = true)
    public long getReviewCount(Book book) {
        return reviewRepository.countByBook(book);
    }

    @Transactional(readOnly = true)
    public boolean hasUserReviewed(User user, Book book) {
        return reviewRepository.existsByUserAndBook(user, book);
    }

    @Transactional(readOnly = true)
    public Review getUserReview(User user, Book book) {
        return reviewRepository.findByUserAndBook(user, book).orElse(null);
    }
}
