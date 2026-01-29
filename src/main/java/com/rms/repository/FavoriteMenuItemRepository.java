package com.rms.repository;

import com.rms.entity.FavoriteMenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteMenuItemRepository extends JpaRepository<FavoriteMenuItem, Long> {

    Optional<FavoriteMenuItem> findByCustomerIdAndMenuItemId(Long customerId, Long menuItemId);

    List<FavoriteMenuItem> findByCustomerId(Long customerId);

    Page<FavoriteMenuItem> findByCustomerId(Long customerId, Pageable pageable);

    List<FavoriteMenuItem> findByCustomerIdAndRestaurantId(Long customerId, Long restaurantId);

    @Query("SELECT f FROM FavoriteMenuItem f WHERE f.customerId = :customerId " +
            "ORDER BY f.orderCount DESC, f.lastOrderedAt DESC")
    List<FavoriteMenuItem> findTopFavoritesByCustomer(@Param("customerId") Long customerId, Pageable pageable);

    @Query("SELECT f FROM FavoriteMenuItem f WHERE f.menuItemId = :menuItemId " +
            "ORDER BY f.orderCount DESC")
    List<FavoriteMenuItem> findTopCustomersByMenuItem(@Param("menuItemId") Long menuItemId, Pageable pageable);

    boolean existsByCustomerIdAndMenuItemId(Long customerId, Long menuItemId);

    void deleteByCustomerIdAndMenuItemId(Long customerId, Long menuItemId);

    @Query("SELECT COUNT(f) FROM FavoriteMenuItem f WHERE f.customerId = :customerId")
    Long countByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT COUNT(f) FROM FavoriteMenuItem f WHERE f.menuItemId = :menuItemId")
    Long countByMenuItemId(@Param("menuItemId") Long menuItemId);
}
