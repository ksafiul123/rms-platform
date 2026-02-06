package com.rms.repository;

import com.rms.entity.CustomerBehavior;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CustomerBehaviorRepository extends JpaRepository<CustomerBehavior, Long> {

    Optional<CustomerBehavior> findByRestaurantIdAndAnalysisDate(Long restaurantId, LocalDate analysisDate);
}
