package com.fitness.fitnessapi.dto;

import com.fitness.fitnessapi.entity.TransactionHistory;
import com.fitness.fitnessapi.enums.TransactionType;

import java.time.LocalDateTime;

public class TransactionHistoryDTO {
    private Long id;
    private Double amount;
    private TransactionType type;
    private Long referenceUserId;
    private LocalDateTime createdAt;
    private Long requestId;

    public TransactionHistoryDTO(TransactionHistory txn) {
        this.id = txn.getId();
        this.amount = txn.getAmount();
        this.type = txn.getType();
        this.referenceUserId = txn.getReferenceUser() != null ? txn.getReferenceUser().getId() : null;
        this.createdAt = txn.getCreatedAt();
        this.requestId = txn.getRequest() != null ? txn.getRequest().getId() : null;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Double getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public Long getReferenceUserId() {
        return referenceUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getRequestId() {
        return requestId;
    }
}
