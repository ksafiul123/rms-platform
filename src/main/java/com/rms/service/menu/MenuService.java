package com.rms.service.menu;

//package com.rms.service.menu;

import com.rms.dto.auth.ApiResponse;
import com.rms.dto.menu.*;
import com.rms.entity.*;
import com.rms.repository.*;
import dev.safi.restaurant_management_system.dto.menu.*;
import dev.safi.restaurant_management_system.entity.*;
import com.rms.enums.ItemType;
import com.rms.enums.SelectionType;
import com.rms.exception.BadRequestException;
import com.rms.exception.ResourceNotFoundException;
import dev.safi.restaurant_management_system.repository.*;
import com.rms.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Menu Service - Manages categories, items, and pricing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final ItemVariantRepository variantRepository;
    private final ModifierGroupRepository modifierGroupRepository;
    private final ModifierOptionRepository modifierOptionRepository;
    private final ItemModifierRepository itemModifierRepository;
    private final PriceScheduleRepository priceScheduleRepository;
    private final IngredientRepository ingredientRepository;
    private final ItemIngredientRepository itemIngredientRepository;

    // ==================== CATEGORY MANAGEMENT ====================

    /**
     * Create menu category
     */
    @Transactional
    @CacheEvict(value = "menuCategories", key = "#restaurantId")
    public ApiResponse<CategoryResponse> createCategory(Long restaurantId, CategoryRequest request) {
        validateRestaurantAccess(restaurantId);

        if (categoryRepository.existsByRestaurantIdAndName(restaurantId, request.getName())) {
            throw new BadRequestException("Category with this name already exists");
        }

        MenuCategory parentCategory = null;
        if (request.getParentCategoryId() != null) {
            parentCategory = categoryRepository.findByIdAndRestaurantId(
                            request.getParentCategoryId(), restaurantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
        }

        MenuCategory category = MenuCategory.builder()
                .restaurantId(restaurantId)
                .name(request.getName())
                .description(request.getDescription())
                .parentCategory(parentCategory)
                .displayOrder(request.getDisplayOrder())
                .imageUrl(request.getImageUrl())
                .iconName(request.getIconName())
                .availableFrom(request.getAvailableFrom())
                .availableTo(request.getAvailableTo())
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .isActive(true)
                .build();

        category = categoryRepository.save(category);

        log.info("Category created: {} for restaurant ID: {}", category.getName(), restaurantId);

        return ApiResponse.success("Category created successfully",
                mapToCategoryResponse(category));
    }

    /**
     * Get all categories for restaurant
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "menuCategories", key = "#restaurantId")
    public ApiResponse<List<CategoryResponse>> getAllCategories(Long restaurantId) {
        validateRestaurantAccess(restaurantId);

        List<MenuCategory> categories = categoryRepository
                .findByRestaurantIdAndIsActiveTrueOrderByDisplayOrderAsc(restaurantId);

        List<CategoryResponse> responses = categories.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Categories fetched successfully", responses);
    }

    /**
     * Get root categories (top-level only)
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<CategoryResponse>> getRootCategories(Long restaurantId) {
        validateRestaurantAccess(restaurantId);

        List<MenuCategory> categories = categoryRepository
                .findByRestaurantIdAndParentCategoryIsNullAndIsActiveTrueOrderByDisplayOrderAsc(restaurantId);

        List<CategoryResponse> responses = categories.stream()
                .map(this::mapToCategoryResponseWithChildren)
                .collect(Collectors.toList());

        return ApiResponse.success("Root categories fetched successfully", responses);
    }

    /**
     * Update category
     */
    @Transactional
    @CacheEvict(value = "menuCategories", key = "#restaurantId")
    public ApiResponse<CategoryResponse> updateCategory(
            Long restaurantId, Long categoryId, CategoryRequest request) {

        validateRestaurantAccess(restaurantId);

        MenuCategory category = categoryRepository.findByIdAndRestaurantId(categoryId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setDisplayOrder(request.getDisplayOrder());
        category.setImageUrl(request.getImageUrl());
        category.setIconName(request.getIconName());
        category.setAvailableFrom(request.getAvailableFrom());
        category.setAvailableTo(request.getAvailableTo());
        category.setIsFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : category.getIsFeatured());

        category = categoryRepository.save(category);

        log.info("Category updated: {} for restaurant ID: {}", category.getName(), restaurantId);

        return ApiResponse.success("Category updated successfully",
                mapToCategoryResponse(category));
    }

    // ==================== MENU ITEM MANAGEMENT ====================

    /**
     * Create menu item
     */
    @Transactional
    @CacheEvict(value = {"menuItems", "menuCategories"}, allEntries = true)
    public ApiResponse<MenuItemResponse> createMenuItem(Long restaurantId, MenuItemRequest request) {
        validateRestaurantAccess(restaurantId);

        MenuCategory category = categoryRepository.findByIdAndRestaurantId(
                        request.getCategoryId(), restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        String sku = generateSku(restaurantId);

        MenuItem menuItem = MenuItem.builder()
                .restaurantId(restaurantId)
                .category(category)
                .sku(sku)
                .name(request.getName())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .discountedPrice(request.getDiscountedPrice())
                .imageUrl(request.getImageUrl())
                .preparationTimeMinutes(request.getPreparationTimeMinutes())
                .itemType(request.getItemType() != null ? ItemType.valueOf(request.getItemType()) : ItemType.FOOD)
                .isVegetarian(request.getIsVegetarian() != null ? request.getIsVegetarian() : false)
                .isVegan(request.getIsVegan() != null ? request.getIsVegan() : false)
                .isGlutenFree(request.getIsGlutenFree() != null ? request.getIsGlutenFree() : false)
                .isSpicy(request.getIsSpicy() != null ? request.getIsSpicy() : false)
                .spiceLevel(request.getSpiceLevel())
                .calories(request.getCalories())
                .allergenInfo(request.getAllergenInfo())
                .availableFrom(request.getAvailableFrom())
                .availableTo(request.getAvailableTo())
                .availableForDineIn(request.getAvailableForDineIn() != null ? request.getAvailableForDineIn() : true)
                .availableForTakeaway(request.getAvailableForTakeaway() != null ? request.getAvailableForTakeaway() : true)
                .availableForDelivery(request.getAvailableForDelivery() != null ? request.getAvailableForDelivery() : true)
                .stockQuantity(request.getStockQuantity())
                .lowStockThreshold(request.getLowStockThreshold())
                .displayOrder(request.getDisplayOrder())
                .isAvailable(true)
                .isActive(true)
                .build();

        menuItem = menuItemRepository.save(menuItem);

        // Create variants if provided
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            createVariants(menuItem, request.getVariants());
        }

        // Link modifier groups if provided
        if (request.getModifierGroupIds() != null && !request.getModifierGroupIds().isEmpty()) {
            linkModifierGroups(menuItem, request.getModifierGroupIds());
        }

        // Create ingredient requirements if provided
        if (request.getIngredients() != null && !request.getIngredients().isEmpty()) {
            createIngredientRequirements(menuItem, request.getIngredients());
        }

        log.info("Menu item created: {} (SKU: {}) for restaurant ID: {}",
                menuItem.getName(), sku, restaurantId);

        return ApiResponse.success("Menu item created successfully",
                mapToMenuItemResponse(menuItem));
    }

    /**
     * Get menu items by category
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<MenuItemResponse>> getItemsByCategory(Long restaurantId, Long categoryId) {
        validateRestaurantAccess(restaurantId);

        List<MenuItem> items = menuItemRepository
                .findByCategoryIdAndIsActiveTrueOrderByDisplayOrderAsc(categoryId);

        List<MenuItemResponse> responses = items.stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Menu items fetched successfully", responses);
    }

    /**
     * Get menu item by ID
     */
    @Transactional(readOnly = true)
    public ApiResponse<MenuItemResponse> getMenuItem(Long restaurantId, Long itemId) {
        validateRestaurantAccess(restaurantId);

        MenuItem item = menuItemRepository.findByIdAndRestaurantId(itemId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        return ApiResponse.success("Menu item fetched successfully",
                mapToMenuItemResponse(item));
    }

    /**
     * Update menu item
     */
    @Transactional
    @CacheEvict(value = {"menuItems", "menuCategories"}, allEntries = true)
    public ApiResponse<MenuItemResponse> updateMenuItem(
            Long restaurantId, Long itemId, MenuItemRequest request) {

        validateRestaurantAccess(restaurantId);

        MenuItem item = menuItemRepository.findByIdAndRestaurantId(itemId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (request.getCategoryId() != null && !request.getCategoryId().equals(item.getCategory().getId())) {
            MenuCategory newCategory = categoryRepository.findByIdAndRestaurantId(
                            request.getCategoryId(), restaurantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            item.setCategory(newCategory);
        }

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setBasePrice(request.getBasePrice());
        item.setDiscountedPrice(request.getDiscountedPrice());
        item.setImageUrl(request.getImageUrl());
        item.setPreparationTimeMinutes(request.getPreparationTimeMinutes());
        item.setIsVegetarian(request.getIsVegetarian());
        item.setIsVegan(request.getIsVegan());
        item.setIsGlutenFree(request.getIsGlutenFree());
        item.setIsSpicy(request.getIsSpicy());
        item.setSpiceLevel(request.getSpiceLevel());
        item.setCalories(request.getCalories());
        item.setAllergenInfo(request.getAllergenInfo());
        item.setDisplayOrder(request.getDisplayOrder());

        item = menuItemRepository.save(item);

        log.info("Menu item updated: {} for restaurant ID: {}", item.getName(), restaurantId);

        return ApiResponse.success("Menu item updated successfully",
                mapToMenuItemResponse(item));
    }

    /**
     * Toggle item availability
     */
    @Transactional
    public ApiResponse<Void> toggleAvailability(Long restaurantId, Long itemId, Boolean isAvailable) {
        validateRestaurantAccess(restaurantId);

        MenuItem item = menuItemRepository.findByIdAndRestaurantId(itemId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        item.setIsAvailable(isAvailable);
        menuItemRepository.save(item);

        log.info("Menu item {} availability set to: {} for restaurant ID: {}",
                item.getName(), isAvailable, restaurantId);

        return ApiResponse.success(
                isAvailable ? "Item marked as available" : "Item marked as unavailable",
                null);
    }

    /**
     * Bulk update availability
     */
    @Transactional
    public ApiResponse<Void> bulkUpdateAvailability(Long restaurantId, BulkAvailabilityRequest request) {
        validateRestaurantAccess(restaurantId);

        menuItemRepository.bulkUpdateAvailability(request.getItemIds(), request.getIsAvailable());

        log.info("Bulk availability update: {} items set to {} for restaurant ID: {}",
                request.getItemIds().size(), request.getIsAvailable(), restaurantId);

        return ApiResponse.success("Availability updated successfully", null);
    }

    /**
     * Get featured items
     */
    @Transactional(readOnly = true)
    public ApiResponse<List<MenuItemResponse>> getFeaturedItems(Long restaurantId) {
        validateRestaurantAccess(restaurantId);

        List<MenuItem> items = menuItemRepository.findFeaturedItems(restaurantId);

        List<MenuItemResponse> responses = items.stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("Featured items fetched successfully", responses);
    }

    /**
     * Get menu statistics
     */
    @Transactional(readOnly = true)
    public ApiResponse<MenuStatsResponse> getMenuStats(Long restaurantId) {
        validateRestaurantAccess(restaurantId);

        Long totalCategories = categoryRepository
                .findByRestaurantIdAndIsActiveTrueOrderByDisplayOrderAsc(restaurantId).stream().count();

        List<MenuItem> allItems = menuItemRepository
                .findByRestaurantIdAndIsActiveTrueOrderByDisplayOrderAsc(restaurantId);

        MenuStatsResponse stats = MenuStatsResponse.builder()
                .totalCategories(totalCategories)
                .totalItems((long) allItems.size())
                .activeItems(allItems.stream().filter(MenuItem::getIsActive).count())
                .unavailableItems(allItems.stream().filter(item -> !item.getIsAvailable()).count())
                .lowStockItems(menuItemRepository.findLowStockItems(restaurantId).stream().count())
                .featuredItems(allItems.stream().filter(MenuItem::getIsFeatured).count())
                .bestSellerItems(allItems.stream().filter(MenuItem::getIsBestSeller).count())
                .averagePrice(menuItemRepository.getAveragePrice(restaurantId))
                .lowestPrice(allItems.stream()
                        .map(MenuItem::getBasePrice)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO))
                .highestPrice(allItems.stream()
                        .map(MenuItem::getBasePrice)
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO))
                .build();

        return ApiResponse.success("Menu statistics fetched successfully", stats);
    }

    // ==================== MODIFIER MANAGEMENT ====================

    /**
     * Create modifier group
     */
    @Transactional
    public ApiResponse<ModifierGroupResponse> createModifierGroup(
            Long restaurantId, ModifierGroupRequest request) {

        validateRestaurantAccess(restaurantId);

        if (modifierGroupRepository.existsByRestaurantIdAndName(restaurantId, request.getName())) {
            throw new BadRequestException("Modifier group with this name already exists");
        }

        ModifierGroup group = ModifierGroup.builder()
                .restaurantId(restaurantId)
                .name(request.getName())
                .description(request.getDescription())
                .selectionType(SelectionType.valueOf(request.getSelectionType()))
                .minSelections(request.getMinSelections())
                .maxSelections(request.getMaxSelections())
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : false)
                .displayOrder(request.getDisplayOrder())
                .isActive(true)
                .build();

        group = modifierGroupRepository.save(group);

        // Create options
        for (ModifierOptionRequest optionReq : request.getOptions()) {
            ModifierOption option = ModifierOption.builder()
                    .modifierGroup(group)
                    .name(optionReq.getName())
                    .description(optionReq.getDescription())
                    .priceAdjustment(optionReq.getPriceAdjustment())
                    .isDefault(optionReq.getIsDefault() != null ? optionReq.getIsDefault() : false)
                    .displayOrder(optionReq.getDisplayOrder())
                    .isAvailable(true)
                    .build();

            modifierOptionRepository.save(option);
        }

        log.info("Modifier group created: {} for restaurant ID: {}", group.getName(), restaurantId);

        return ApiResponse.success("Modifier group created successfully",
                mapToModifierGroupResponse(group));
    }

    // ==================== HELPER METHODS ====================

    private void createVariants(MenuItem menuItem, List<ItemVariantRequest> variantRequests) {
        for (ItemVariantRequest variantReq : variantRequests) {
            ItemVariant variant = ItemVariant.builder()
                    .menuItem(menuItem)
                    .name(variantReq.getName())
                    .sku(menuItem.getSku() + "-" + variantReq.getName().toUpperCase().substring(0, 1))
                    .priceAdjustment(variantReq.getPriceAdjustment())
                    .isDefault(variantReq.getIsDefault() != null ? variantReq.getIsDefault() : false)
                    .displayOrder(variantReq.getDisplayOrder())
                    .isAvailable(true)
                    .build();

            variantRepository.save(variant);
        }
    }

    private void linkModifierGroups(MenuItem menuItem, List<Long> modifierGroupIds) {
        for (Long groupId : modifierGroupIds) {
            ModifierGroup group = modifierGroupRepository.findById(groupId)
                    .orElseThrow(() -> new ResourceNotFoundException("Modifier group not found: " + groupId));

            ItemModifier itemModifier = ItemModifier.builder()
                    .menuItem(menuItem)
                    .modifierGroup(group)
                    .isRequired(group.getIsRequired())
                    .displayOrder(group.getDisplayOrder())
                    .build();

            itemModifierRepository.save(itemModifier);
        }
    }

    private void createIngredientRequirements(MenuItem menuItem, List<ItemIngredientRequest> ingredientRequests) {
        for (ItemIngredientRequest ingredientReq : ingredientRequests) {
            Ingredient ingredient = ingredientRepository.findById(ingredientReq.getIngredientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));

            ItemIngredient itemIngredient = ItemIngredient.builder()
                    .menuItem(menuItem)
                    .ingredient(ingredient)
                    .quantity(ingredientReq.getQuantity())
                    .unit(ingredientReq.getUnit())
                    .build();

            itemIngredientRepository.save(itemIngredient);
        }
    }

    private String generateSku(Long restaurantId) {
        String prefix = "ITEM";
        long count = menuItemRepository.countActiveItems(restaurantId);
        return String.format("%s-%d-%05d", prefix, restaurantId, count + 1);
    }

    private void validateRestaurantAccess(Long restaurantId) {
        if (!SecurityUtil.isSuperAdmin()) {
            Long userRestaurantId = SecurityUtil.getCurrentRestaurantId();
            if (userRestaurantId == null || !userRestaurantId.equals(restaurantId)) {
                throw new SecurityException("Access denied to this restaurant");
            }
        }
    }

    private CategoryResponse mapToCategoryResponse(MenuCategory category) {
        Long itemCount = categoryRepository.countItemsByCategory(category.getId());

        return CategoryResponse.builder()
                .id(category.getId())
                .restaurantId(category.getRestaurantId())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .iconName(category.getIconName())
                .displayOrder(category.getDisplayOrder())
                .parentCategoryId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                .parentCategoryName(category.getParentCategory() != null ? category.getParentCategory().getName() : null)
                .itemCount(itemCount.intValue())
                .isActive(category.getIsActive())
                .isFeatured(category.getIsFeatured())
                .availableFrom(category.getAvailableFrom())
                .availableTo(category.getAvailableTo())
                .createdAt(category.getCreatedAt())
                .build();
    }

    private CategoryResponse mapToCategoryResponseWithChildren(MenuCategory category) {
        CategoryResponse response = mapToCategoryResponse(category);

        if (!category.getSubCategories().isEmpty()) {
            List<CategoryResponse> children = category.getSubCategories().stream()
                    .filter(MenuCategory::getIsActive)
                    .map(this::mapToCategoryResponse)
                    .collect(Collectors.toList());
            response.setSubCategories(children);
        }

        return response;
    }

    private MenuItemResponse mapToMenuItemResponse(MenuItem item) {
        BigDecimal finalPrice = item.getDiscountedPrice() != null ?
                item.getDiscountedPrice() : item.getBasePrice();

        Boolean isLowStock = item.getStockQuantity() != null &&
                item.getLowStockThreshold() != null &&
                item.getStockQuantity() <= item.getLowStockThreshold();

        return MenuItemResponse.builder()
                .id(item.getId())
                .restaurantId(item.getRestaurantId())
                .categoryId(item.getCategory().getId())
                .categoryName(item.getCategory().getName())
                .sku(item.getSku())
                .name(item.getName())
                .description(item.getDescription())
                .basePrice(item.getBasePrice())
                .discountedPrice(item.getDiscountedPrice())
                .finalPrice(finalPrice)
                .imageUrl(item.getImageUrl())
                .preparationTimeMinutes(item.getPreparationTimeMinutes())
                .itemType(item.getItemType() != null ? item.getItemType().name() : null)
                .isVegetarian(item.getIsVegetarian())
                .isVegan(item.getIsVegan())
                .isGlutenFree(item.getIsGlutenFree())
                .isSpicy(item.getIsSpicy())
                .spiceLevel(item.getSpiceLevel())
                .calories(item.getCalories())
                .allergenInfo(item.getAllergenInfo())
                .isAvailable(item.getIsAvailable())
                .isActive(item.getIsActive())
                .isFeatured(item.getIsFeatured())
                .isBestSeller(item.getIsBestSeller())
                .availableFrom(item.getAvailableFrom())
                .availableTo(item.getAvailableTo())
                .availableForDineIn(item.getAvailableForDineIn())
                .availableForTakeaway(item.getAvailableForTakeaway())
                .availableForDelivery(item.getAvailableForDelivery())
                .stockQuantity(item.getStockQuantity())
                .lowStockThreshold(item.getLowStockThreshold())
                .isLowStock(isLowStock)
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private ModifierGroupResponse mapToModifierGroupResponse(ModifierGroup group) {
        List<ModifierOption> options = modifierOptionRepository.findByModifierGroupId(group.getId());

        return ModifierGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .selectionType(group.getSelectionType().name())
                .minSelections(group.getMinSelections())
                .maxSelections(group.getMaxSelections())
                .isRequired(group.getIsRequired())
                .displayOrder(group.getDisplayOrder())
                .options(options.stream()
                        .map(this::mapToModifierOptionResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private ModifierOptionResponse mapToModifierOptionResponse(ModifierOption option) {
        return ModifierOptionResponse.builder()
                .id(option.getId())
                .name(option.getName())
                .description(option.getDescription())
                .priceAdjustment(option.getPriceAdjustment())
                .isDefault(option.getIsDefault())
                .isAvailable(option.getIsAvailable())
                .displayOrder(option.getDisplayOrder())
                .build();
    }
}
