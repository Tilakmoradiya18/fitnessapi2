package com.fitness.fitnessapi.service;

import com.fitness.fitnessapi.dto.ApiSuccessResponse;
import com.fitness.fitnessapi.dto.WalletActionRequest;
import com.fitness.fitnessapi.entity.TransactionHistory;
import com.fitness.fitnessapi.entity.User;
import com.fitness.fitnessapi.entity.Wallet;
import com.fitness.fitnessapi.enums.TransactionType;
import com.fitness.fitnessapi.repository.TransactionHistoryRepository;
import com.fitness.fitnessapi.repository.UserRepository;
import com.fitness.fitnessapi.repository.WalletRepository;
import com.fitness.fitnessapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

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


}

