package com.rms.repository;

//package com.rms.repository;

import com.rms.entity.Permission;
import com.rms.enums.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Permission Repository
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    List<Permission> findByCategory(String category);

    List<Permission> findByResource(String resource);

    List<Permission> findByIsActiveTrueOrderByCategoryAsc();

    Boolean existsByName(String name);

    @Query("SELECT p FROM Permission p WHERE p.name IN :names")
    List<Permission> findByNames(@Param("names") Set<String> names);

    @Query("SELECT DISTINCT p.category FROM Permission p WHERE p.category IS NOT NULL ORDER BY p.category")
    List<String> findAllCategories();

    @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.action = :action")
    Optional<Permission> findByResourceAndAction(
            @Param("resource") String resource,
            @Param("action") ActionType action
    );
}

