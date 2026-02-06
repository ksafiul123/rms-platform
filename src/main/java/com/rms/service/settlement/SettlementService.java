package com.rms.service.settlement;

import com.rms.dto.settlement.PayoutRequest;
import com.rms.entity.OrderSettlement;
import com.rms.entity.Restaurant;
import com.rms.entity.RestaurantPayout;
import com.rms.exception.BadRequestException;
import com.rms.exception.ResourceNotFoundException;
import com.rms.repository.OrderSettlementRepository;
import com.rms.repository.RestaurantPayoutRepository;
import com.rms.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(
        isolation = Isolation.READ_COMMITTED,
        timeout = 60,
        rollbackFor = Exception.class
)
public class SettlementService {

    private final OrderSettlementRepository settlementRepository;
    private final RestaurantPayoutRepository payoutRepository;
    private final RestaurantRepository restaurantRepository;

    @Retryable(
            value = {OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public RestaurantPayout processPayout(Long restaurantId, PayoutRequest request) {
        List<OrderSettlement> settlements = settlementRepository
                .findPendingSettlementsForPeriod(restaurantId, request.getPeriodStart(),
                        request.getPeriodEnd());

        if (settlements.isEmpty()) {
            throw new BadRequestException("No pending settlements for payout");
        }

        BigDecimal totalAmount = settlements.stream()
                .map(OrderSettlement::getNetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        RestaurantPayout payout = RestaurantPayout.builder()
                .restaurant(getRestaurant(restaurantId))
                .payoutReference(generatePayoutReference())
                .payoutType(resolvePayoutType(request))
                .periodStartDate(request.getPeriodStart())
                .periodEndDate(request.getPeriodEnd())
                .totalOrders(settlements.size())
                .totalOrderAmount(sumAmount(settlements, OrderSettlement::getOrderAmount))
                .totalCommission(sumAmount(settlements, OrderSettlement::getCommissionAmount))
                .totalFees(sumAmount(settlements, OrderSettlement::getPlatformFee))
                .totalRefunds(sumAmount(settlements, OrderSettlement::getRefundAmount))
                .totalAdjustments(sumAmount(settlements, OrderSettlement::getAdjustmentAmount))
                .payoutAmount(totalAmount)
                .payoutMethod(resolvePayoutMethod(request))
                .payoutStatus(RestaurantPayout.PayoutStatus.PENDING_APPROVAL)
                .initiatedAt(LocalDateTime.now())
                .build();

        payoutRepository.save(payout);

        settlements.forEach(settlement -> {
            settlement.setPayout(payout);
            settlement.setSettlementStatus(OrderSettlement.SettlementStatus.IN_PROGRESS);
        });

        settlementRepository.saveAll(settlements);

        return payout;
    }

    private Restaurant getRestaurant(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
    }

    private RestaurantPayout.PayoutMethod resolvePayoutMethod(PayoutRequest request) {
        if (request.getPayoutMethod() != null) {
            return request.getPayoutMethod();
        }
        return RestaurantPayout.PayoutMethod.BANK_TRANSFER;
    }

    private RestaurantPayout.PayoutType resolvePayoutType(PayoutRequest request) {
        if (request.getPayoutType() != null) {
            return request.getPayoutType();
        }
        return RestaurantPayout.PayoutType.ON_DEMAND;
    }

    private BigDecimal sumAmount(List<OrderSettlement> settlements,
                                 java.util.function.Function<OrderSettlement, BigDecimal> mapper) {
        return settlements.stream()
                .map(mapper)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generatePayoutReference() {
        return "PAYOUT-" + UUID.randomUUID();
    }
}
