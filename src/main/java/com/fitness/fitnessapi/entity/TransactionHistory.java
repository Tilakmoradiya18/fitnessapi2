package com.fitness.fitnessapi.entity;

import com.fitness.fitnessapi.enums.TransactionType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_history")
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type; // ADD, WITHDRAW

    @ManyToOne
    @JoinColumn(name = "reference_user_id")
    private User referenceUser; // for sender/receiver (nullable)

    @Column(name = "is_confirmed")
    private boolean confirmed = false;

    private LocalDateTime createdAt;

    private String otp; // Add this field

    private boolean completed; // true if credited or refunded


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public User getReferenceUser() {
        return referenceUser;
    }

    public void setReferenceUser(User referenceUser) {
        this.referenceUser = referenceUser;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
