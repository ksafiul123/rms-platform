package dev.safi.restaurant_management_system.repository;

import dev.safi.restaurant_management_system.entity.Salesman;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Salesman Repository
 */
@Repository
public interface SalesmanRepository extends JpaRepository<Salesman, Long> {

    Optional<Salesman> findByUserId(Long userId);

    Optional<Salesman> findBySalesmanCode(String salesmanCode);

    List<Salesman> findByIsActiveTrue();

    Boolean existsByUserId(Long userId);

    Boolean existsBySalesmanCode(String salesmanCode);

    @Query("SELECT s FROM Salesman s WHERE s.isActive = true ORDER BY s.totalOnboarded DESC")
    List<Salesman> findTopPerformers(Pageable pageable);

    @Modifying
    @Query("UPDATE Salesman s SET s.totalOnboarded = s.totalOnboarded + 1 WHERE s.id = :salesmanId")
    void incrementTotalOnboarded(@Param("salesmanId") Long salesmanId);

    @Modifying
    @Query("UPDATE Salesman s SET s.totalActive = s.totalActive + 1 WHERE s.id = :salesmanId")
    void incrementTotalActive(@Param("salesmanId") Long salesmanId);

    @Modifying
    @Query("UPDATE Salesman s SET s.totalActive = s.totalActive - 1 WHERE s.id = :salesmanId AND s.totalActive > 0")
    void decrementTotalActive(@Param("salesmanId") Long salesmanId);
}
