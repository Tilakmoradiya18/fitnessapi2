package com.fitness.fitnessapi.dto;

import java.util.List;

public class TransactionHistoryResponse {
    private Long userId;
    private String userName;
    private List<TransactionHistoryDTO> transactions;

    public TransactionHistoryResponse(Long userId, String userName, List<TransactionHistoryDTO> transactions) {
        this.userId = userId;
        this.userName = userName;
        this.transactions = transactions;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public List<TransactionHistoryDTO> getTransactions() {
        return transactions;
    }
}
