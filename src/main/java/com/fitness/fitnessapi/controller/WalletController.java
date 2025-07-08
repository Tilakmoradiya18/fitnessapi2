package com.fitness.fitnessapi.controller;

import com.fitness.fitnessapi.dto.ApiSuccessResponse;
import com.fitness.fitnessapi.dto.WalletActionRequest;
import com.fitness.fitnessapi.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

