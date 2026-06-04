package com.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fines")
@Data
@NoArgsConstructor
public class Fine {

    public enum Status { PENDING, PARTIAL, PAID, WAIVED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_record_id", nullable = false, unique = true)
    private BorrowRecord borrowRecord;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "paid_amount", precision = 12, scale = 0)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "fine_date")
    private LocalDate fineDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        fineDate = LocalDate.now();
    }

    public BigDecimal getRemainingAmount() {
        return amount.subtract(paidAmount);
    }
}
