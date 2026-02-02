package com.rms.repository;

import com.rms.entity.DisplayConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DisplayConfigurationRepository extends JpaRepository<DisplayConfiguration, Long> {

    Optional<DisplayConfiguration> findByRestaurantId(Long restaurantId);

    Optional<DisplayConfiguration> findByDisplayToken(String displayToken);

    List<DisplayConfiguration> findByIsActiveTrue();

    boolean existsByRestaurantId(Long restaurantId);
}

