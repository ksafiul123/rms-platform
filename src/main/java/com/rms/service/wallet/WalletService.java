package com.rms.service.wallet;

import com.rms.entity.CustomerWallet;
import com.rms.entity.WalletTransaction;
import com.rms.exception.InsufficientFundsException;
import com.rms.exception.ResourceNotFoundException;
import com.rms.repository.CustomerWalletRepository;
import com.rms.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE, timeout = 5)
public class WalletService {

    private final CustomerWalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public void debitWallet(Long userId, BigDecimal amount, String description) {
        CustomerWallet wallet = walletRepository
                .findByCustomerIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient wallet balance");
        }

        BigDecimal balanceBefore = wallet.getBalance();
        wallet.setBalance(balanceBefore.subtract(amount));
        wallet.setTotalDebited(wallet.getTotalDebited().add(amount));
        wallet.setLastTransactionAt(LocalDateTime.now());

        WalletTransaction txn = WalletTransaction.builder()
                .wallet(wallet)
                .transactionType(WalletTransaction.TransactionType.DEBIT)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(wallet.getBalance())
                .transactionTimestamp(LocalDateTime.now())
                .description(description)
                .build();

        walletTransactionRepository.save(txn);
        walletRepository.save(wallet);
    }
}
