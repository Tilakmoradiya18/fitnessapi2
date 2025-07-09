package com.fitness.fitnessapi.controller;

import com.fitness.fitnessapi.dto.ApiSuccessResponse;
import com.fitness.fitnessapi.dto.ConfirmPaymentDTO;
import com.fitness.fitnessapi.dto.WalletActionRequest;
import com.fitness.fitnessapi.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @PostMapping("/add")
    public ResponseEntity<ApiSuccessResponse> addMoney(@RequestBody WalletActionRequest request,
                                                       HttpServletRequest httpRequest) {
        return ResponseEntity.ok(walletService.addMoney(request, httpRequest));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiSuccessResponse> withdraw(@RequestBody WalletActionRequest request,
                                                       HttpServletRequest httpRequest) {
        return ResponseEntity.ok(walletService.withdrawMoney(request, httpRequest));
    }

    @GetMapping("/balance")
    public ApiSuccessResponse getWalletBalance(HttpServletRequest request) {
        return walletService.getWalletBalance(request);
    }

    @PostMapping("/confirm-payment")
    public ApiSuccessResponse confirmPayment(@RequestBody ConfirmPaymentDTO dto) {
        return walletService.confirmPayment(dto);
    }

}

