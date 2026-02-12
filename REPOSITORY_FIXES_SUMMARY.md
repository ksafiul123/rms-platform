# Repository Fixes Summary

## Problem
Spring Data JPA derived query methods were failing because they were trying to access fields like `menuItemId` when the entities only had relationship fields like `MenuItem menuItem`.

## Solution
Added explicit `@Query` annotations with JPQL that properly navigates through entity relationships using dot notation (e.g., `menuItem.id` instead of `menuItemId`).

## Fixed Repositories

### 1. MenuItemInventoryRepository
- `findByMenuItemId` - Fixed
- `findByInventoryItemId` - Fixed  
- `existsByMenuItemIdAndInventoryItemId` - Fixed
- `deleteByMenuItemId` - Fixed with `@Modifying` and `@Transactional`

### 2. PriceScheduleRepository
- `findByMenuItemId` - Fixed

### 3. WalletTransactionRepository
- `findByWalletIdOrderByTransactionTimestampDesc` - Fixed
- `findByPaymentId` - Fixed
- `findByOrderId` - Fixed

### 4. PaymentSplitDetailRepository
- `findByParentPaymentId` - Fixed

### 5. PaymentRepository
- `findByOrderId` - Fixed
- `findByCustomerIdOrderByCreatedAtDesc` - Fixed

### 6. OrderSettlementRepository
- `findByOrderId` - Fixed
- `findByRestaurantIdOrderBySettlementDateDesc` - Fixed
- `findByRestaurantIdAndSettlementStatus` - Fixed

### 7. OrderPreparationMetricsRepository
- `findByOrderId` - Fixed

### 8. RefreshTokenRepository
- `findByUserId` - Fixed

### 9. UserNotificationPreferenceRepository
- `findByUserId` - Fixed

### 10. TableSessionGuestRepository
- `findBySessionIdAndUserId` - Fixed
- `findBySessionIdAndStatus` - Fixed
- `existsBySessionIdAndUserId` - Fixed

### 11. SettlementAdjustmentRepository
- `findBySettlementId` - Fixed

### 12. OrderDisplaySnapshotRepository
- `findByOrderId` - Fixed

### 13. ModifierOptionRepository
- `findByModifierGroupId` - Fixed
- `findByModifierGroupIdAndIsAvailableTrue` - Fixed
- `findByModifierGroupIdAndIsDefaultTrue` - Fixed

### 14. ModifierGroupRepository
- Removed invalid `LEFT JOIN FETCH mg.options` (options field doesn't exist)

### 15. KitchenOrderItemRepository
- `findByOrderId` - Fixed
- `findByOrderIdAndStatus` - Fixed
- `findByAssignedChefId` - Fixed

### 16. ItemVariantRepository
- `findByMenuItemId` - Fixed
- `findByMenuItemIdAndIsAvailableTrue` - Fixed
- `findByMenuItemIdAndIsDefaultTrue` - Fixed

### 17. ItemModifierRepository
- `findByMenuItemId` - Fixed
- `existsByMenuItemIdAndModifierGroupId` - Fixed
- `deleteByMenuItemIdAndModifierGroupId` - Fixed with `@Modifying` and `@Transactional`
- Removed invalid `LEFT JOIN FETCH mg.options`

### 18. ItemIngredientRepository
- `findByMenuItemId` - Fixed
- `deleteByMenuItemId` - Fixed with `@Modifying` and `@Transactional`

### 19. DeliveryAssignmentRepository
- `findByOrderId` - Fixed
- `findByDeliveryPartnerId` - Fixed

### 20. GamePlayerRepository
- `findByGameSessionId` - Fixed
- `findByUserIdOrderByJoinedAtDesc` - Fixed

### 21. GameSessionRepository
- `findByTableSessionId` - Fixed

## IntelliJ IDEA Troubleshooting

If you're still seeing errors in IntelliJ IDEA, try these steps:

### 1. Invalidate Caches and Restart
```
File → Invalidate Caches → Check all options → Invalidate and Restart
```

### 2. Reimport Maven Project
```
Right-click on pom.xml → Maven → Reload Project
```

### 3. Rebuild Project
```
Build → Rebuild Project
```

### 4. Clean and Compile
```
mvn clean compile
```

### 5. Check Maven Settings
- Ensure Maven is properly configured in IntelliJ
- File → Settings → Build, Execution, Deployment → Build Tools → Maven
- Verify Maven home directory is correct

### 6. Enable Annotation Processing
```
File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
Check "Enable annotation processing"
```

### 7. Update Generated Sources
```
Build → Build Project (Ctrl+F9)
```

## Verification

To verify the fixes work:

```bash
# Clean build
mvn clean install -DskipTests

# Run the application
mvn spring-boot:run
```

The application should start without the `Could not resolve attribute` errors.

## Pattern for Future Fixes

If you encounter similar issues with other repositories:

1. Check if the entity has a direct field (e.g., `Long menuItemId`) or a relationship (e.g., `MenuItem menuItem`)
2. If it's a relationship, add an explicit `@Query` annotation:

```java
@Query("SELECT e FROM EntityName e WHERE e.relationshipField.id = :id")
List<EntityName> findByRelationshipFieldId(@Param("id") Long id);
```

3. For delete methods, add `@Modifying` and `@Transactional`:

```java
@Modifying
@Transactional
@Query("DELETE FROM EntityName e WHERE e.relationshipField.id = :id")
void deleteByRelationshipFieldId(@Param("id") Long id);
```

## Notes

- Entities with direct ID fields (like `Salesman.userId`, `RestaurantSettings.restaurantId`) don't need fixes
- Only entities with `@ManyToOne`, `@OneToOne`, or `@OneToMany` relationships need explicit queries
- Always use `@Param` annotations to match parameter names in JPQL queries
