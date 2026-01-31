package com.rms.repository;

import com.rms.entity.CommissionTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommissionTierRepository extends JpaRepository<CommissionTier, Long> {

    List<CommissionTier> findByCommissionRuleIdOrderByTierOrderAsc(Long commissionRuleId);
}
