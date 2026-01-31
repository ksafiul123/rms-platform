package com.rms.repository;

import com.rms.entity.CustomerWallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface CustomerWalletRepository extends JpaRepository<CustomerWallet, Long> {

    Optional<CustomerWallet> findByCustomerIdAndRestaurantIdIsNull(Long customerId);

    Optional<CustomerWallet> findByCustomerIdAndRestaurantId(Long customerId, Long restaurantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM CustomerWallet w WHERE w.customer.id = :customerId")
    Optional<CustomerWallet> findByCustomerIdForUpdate(@Param("customerId") Long customerId);

    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END " +
            "FROM CustomerWallet w " +
            "WHERE w.customer.id = :customerId " +
            "AND w.balance >= :amount")
    boolean hasSufficientBalance(
            @Param("customerId") Long customerId,
            @Param("amount") BigDecimal amount);
}
