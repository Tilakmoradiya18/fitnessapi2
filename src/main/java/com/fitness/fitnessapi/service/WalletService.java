package com.fitness.fitnessapi.service;

import com.fitness.fitnessapi.dto.*;
import com.fitness.fitnessapi.entity.TransactionHistory;
import com.fitness.fitnessapi.entity.User;
import com.fitness.fitnessapi.entity.Wallet;
import com.fitness.fitnessapi.enums.TransactionType;
import com.fitness.fitnessapi.repository.TransactionHistoryRepository;
import com.fitness.fitnessapi.repository.UserRepository;
import com.fitness.fitnessapi.repository.WalletRepository;
import com.fitness.fitnessapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WalletService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionHistoryRepository transactionHistoryRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String DUMMY_OTP = "0000";

    public ApiSuccessResponse addMoney(WalletActionRequest request, HttpServletRequest httpRequest) {
        validateOtp(request.getOtp());

        User user = getLoggedInUser(httpRequest);
        Wallet wallet = walletRepository.findByUser(user).orElse(null);

        if (wallet == null) {
            wallet = new Wallet();
            wallet.setUser(user);
            wallet.setBalance(0.0);
        }

        wallet.setBalance(wallet.getBalance() + request.getAmount());
        wallet.setLastUpdate(LocalDateTime.now());
        walletRepository.save(wallet); // ✅ Save update

        // Save transaction log
        TransactionHistory txn = new TransactionHistory();
        txn.setUser(user);
        txn.setAmount(request.getAmount());
        txn.setType(TransactionType.ADD);
        txn.setCreatedAt(LocalDateTime.now());
        transactionHistoryRepository.save(txn);

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                request.getAmount() + "$ added to wallet successfully.",
                null
        );
    }

    public ApiSuccessResponse withdrawMoney(WalletActionRequest request, HttpServletRequest httpRequest) {
        validateOtp(request.getOtp());

        User user = getLoggedInUser(httpRequest);
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance() < request.getAmount()) {
            throw new RuntimeException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance() - request.getAmount());
        wallet.setLastUpdate(LocalDateTime.now());
        walletRepository.save(wallet);

        TransactionHistory txn = new TransactionHistory();
        txn.setUser(user);
        txn.setAmount(request.getAmount());
        txn.setType(TransactionType.WITHDRAW);
        txn.setCreatedAt(LocalDateTime.now());
        transactionHistoryRepository.save(txn);

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                request.getAmount() + "$ withdrawn from wallet successfully.",
                null
        );

    }

    private User getLoggedInUser(HttpServletRequest request) {
        String email = jwtUtil.extractUsername(jwtUtil.extractToken(request));
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void validateOtp(String otp) {
        if (!DUMMY_OTP.equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }
    }

    public ApiSuccessResponse getWalletBalance(HttpServletRequest httpRequest) {
        String email = jwtUtil.extractUsername(jwtUtil.extractToken(httpRequest));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        Map<String, Object> data = Map.of(
                "balance", wallet.getBalance(),
                "lastUpdate", wallet.getLastUpdate()
        );

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Wallet balance fetched successfully.",
                data
        );
    }


    @Transactional
    public ApiSuccessResponse confirmPayment(ConfirmPaymentDTO dto) {
        TransactionHistory txn = transactionHistoryRepository
                .findByRequestIdAndTypeAndOtp(dto.getRequestId(), TransactionType.BLOCKED, dto.getOtp())
                .orElseThrow(() -> new RuntimeException("Invalid OTP or request"));

        if (txn.isCompleted()) {
            throw new RuntimeException("Transaction already completed.");
        }

        // ✅ Update BLOCKED transaction
        txn.setCompleted(true);
        txn.setType(TransactionType.DEBIT); // <- change BLOCKED to DEBIT
        transactionHistoryRepository.save(txn);

        // ✅ Credit partner
        User receiver = txn.getReferenceUser();
        Wallet partnerWallet = walletRepository.findByUser(receiver)
                .orElseGet(() -> {
                    Wallet wallet = new Wallet();
                    wallet.setUser(receiver);
                    wallet.setBalance(0.0);
                    wallet.setLastUpdate(LocalDateTime.now());
                    return wallet;
                });

        partnerWallet.setBalance(partnerWallet.getBalance() + txn.getAmount());
        partnerWallet.setLastUpdate(LocalDateTime.now());
        walletRepository.save(partnerWallet);

        // ✅ Credit transaction entry
        TransactionHistory creditTxn = new TransactionHistory();
        creditTxn.setUser(receiver);
        creditTxn.setReferenceUser(txn.getUser());
        creditTxn.setRequest(txn.getRequest());
        creditTxn.setAmount(txn.getAmount());
        creditTxn.setType(TransactionType.CREDIT);
        creditTxn.setCreatedAt(LocalDateTime.now());
        creditTxn.setCompleted(true);
        transactionHistoryRepository.save(creditTxn);

        return new ApiSuccessResponse(
                LocalDateTime.now(),
                200,
                "Payment confirmed and credited to partner successfully.",
                Map.of(
                        "creditedAmount", txn.getAmount(),
                        "partnerId", receiver.getId()
                )
        );
    }


    public Map<String, Object> getOtpByRequestId(Long requestId) {
        TransactionHistory txn = transactionHistoryRepository
                .findByRequestIdAndType(requestId, TransactionType.BLOCKED)
                .orElseThrow(() -> new RuntimeException("OTP not found for given requestId"));

        if (txn.isCompleted()) {
            throw new RuntimeException("Transaction already completed.");
        }

        return Map.of(
                "requestId", requestId,
                "otp", txn.getOtp(),
                "createdAt", txn.getCreatedAt()
        );
    }

    public Map<String, Object> getUserTransactionHistory(String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        List<TransactionHistory> transactions = transactionHistoryRepository.findByUserOrderByCreatedAtDesc(user);

        List<TransactionHistoryDTO> transactionDTOs = transactions.stream()
                .map(TransactionHistoryDTO::new)
                .collect(Collectors.toList());

        TransactionHistoryResponse response = new TransactionHistoryResponse(
                user.getId(),
                user.getName(),
                transactionDTOs
        );

        return Map.of(
                "status", 200,
                "message", "Transaction history fetched successfully.",
                "data", response
        );
    }

}

