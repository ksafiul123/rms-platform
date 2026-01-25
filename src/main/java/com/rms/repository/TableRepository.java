package com.rms.repository;

//package com.rms.repository;

import com.rms.entity.Table;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableRepository extends JpaRepository<Table, Long> {

    Optional<Table> findByIdAndRestaurantId(Long id, Long restaurantId);

    Optional<Table> findByQrCode(String qrCode);

    Optional<Table> findByRestaurantIdAndTableNumber(Long restaurantId, String tableNumber);

    Page<Table> findByRestaurantId(Long restaurantId, Pageable pageable);

    Page<Table> findByRestaurantIdAndStatus(Long restaurantId, Table.TableStatus status, Pageable pageable);

    List<Table> findByRestaurantIdAndIsActive(Long restaurantId, Boolean isActive);

    @Query("SELECT t FROM Table t WHERE t.restaurantId = :restaurantId " +
            "AND t.branchId = :branchId AND t.isActive = true")
    List<Table> findActiveTablesByBranch(
            @Param("restaurantId") Long restaurantId,
            @Param("branchId") Long branchId
    );

    boolean existsByQrCode(String qrCode);

    boolean existsByRestaurantIdAndTableNumber(Long restaurantId, String tableNumber);

    @Query("SELECT COUNT(t) FROM Table t WHERE t.restaurantId = :restaurantId " +
            "AND t.status = :status")
    Long countByRestaurantIdAndStatus(
            @Param("restaurantId") Long restaurantId,
            @Param("status") Table.TableStatus status
    );
}

