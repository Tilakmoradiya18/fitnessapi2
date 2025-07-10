package com.fitness.fitnessapi.scheduler;

import com.fitness.fitnessapi.entity.TransactionHistory;
import com.fitness.fitnessapi.entity.User;
import com.fitness.fitnessapi.entity.Wallet;
import com.fitness.fitnessapi.enums.TransactionType;
import com.fitness.fitnessapi.repository.TransactionHistoryRepository;
import com.fitness.fitnessapi.repository.WalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentScheduler {

    private final TransactionHistoryRepository transactionHistoryRepository;
    private final WalletRepository walletRepository;

    public PaymentScheduler(TransactionHistoryRepository transactionHistoryRepository,
                            WalletRepository walletRepository) {
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.walletRepository = walletRepository;
    }

    @Scheduled(cron = "0 0 * * * *") // every hour
    @Transactional
    public void refundUnclaimedPayments() {
        List<TransactionHistory> expiredTransactions = transactionHistoryRepository
                .findAllByTypeAndCompletedFalseAndConfirmedFalseAndCreatedAtBefore(
                        TransactionType.BLOCKED,
                        LocalDateTime.now().minusHours(24)
                );

        for (TransactionHistory txn : expiredTransactions) {
            User sender = txn.getUser();

            // ✅ Refund the amount to sender's wallet
            Wallet senderWallet = walletRepository.findByUser(sender)
                    .orElseThrow(() -> new RuntimeException("Sender wallet not found"));

            senderWallet.setBalance(senderWallet.getBalance() + txn.getAmount());
            senderWallet.setLastUpdate(LocalDateTime.now());
            walletRepository.save(senderWallet);

            // ✅ Update the existing BLOCKED transaction to mark it as REFUNDED
            txn.setType(TransactionType.REFUND);
            txn.setCompleted(true);
            transactionHistoryRepository.save(txn);
        }

        System.out.println("✅ Auto-refunded expired unconfirmed payments.");
    }

}
