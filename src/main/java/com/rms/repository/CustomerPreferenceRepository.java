package com.rms.repository;

//package com.rms.repository;

import com.rms.entity.CustomerPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerPreferenceRepository extends JpaRepository<CustomerPreference, Long> {

    Optional<CustomerPreference> findByCustomerId(Long customerId);

    @Query("SELECT cp FROM CustomerPreference cp WHERE cp.customerId IN " +
            "(SELECT DISTINCT o.customerId FROM Order o WHERE o.restaurantId = :restaurantId) " +
            "AND cp.visibleToChefs = true")
    List<CustomerPreference> findVisiblePreferencesByRestaurant(@Param("restaurantId") Long restaurantId);

    boolean existsByCustomerId(Long customerId);
}

