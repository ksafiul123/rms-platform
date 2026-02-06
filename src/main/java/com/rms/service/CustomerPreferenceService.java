package com.rms.service;

//package com.rms.service;

import com.rms.dto.PreferenceDTO.*;
import com.rms.entity.*;
import com.rms.exception.BadRequestException;
import com.rms.exception.ForbiddenException;
import com.rms.exception.ResourceNotFoundException;
import com.rms.repository.*;
import com.rms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerPreferenceService {

    private final CustomerPreferenceRepository preferenceRepository;
    private final FavoriteMenuItemRepository favoriteRepository;
    private final MenuItemPreferenceRepository menuItemPreferenceRepository;
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;

    @Transactional
    public CustomerPreferenceResponse updatePreferences(UpdateCustomerPreferenceRequest request,
                                                        UserPrincipal currentUser) {
        log.info("Updating preferences for customer {}", currentUser.getId());

        if (!currentUser.hasRole("CUSTOMER")) {
            throw new ForbiddenException("Only customers can manage their preferences");
        }

        CustomerPreference preference = preferenceRepository
                .findByCustomerId(currentUser.getId())
                .orElse(new CustomerPreference());

        preference.setCustomerId(currentUser.getId());

        if (request.getSpiceLevel() != null)
            preference.setSpiceLevel(request.getSpiceLevel());
        if (request.getSweetnessLevel() != null)
            preference.setSweetnessLevel(request.getSweetnessLevel());
        if (request.getSaltLevel() != null)
            preference.setSaltLevel(request.getSaltLevel());
        if (request.getCookingPreference() != null)
            preference.setCookingPreference(request.getCookingPreference());
        if (request.getTemperaturePreference() != null)
            preference.setTemperaturePreference(request.getTemperaturePreference());

        if (request.getIsVegetarian() != null)
            preference.setIsVegetarian(request.getIsVegetarian());
        if (request.getIsVegan() != null)
            preference.setIsVegan(request.getIsVegan());
        if (request.getIsGlutenFree() != null)
            preference.setIsGlutenFree(request.getIsGlutenFree());
        if (request.getIsDairyFree() != null)
            preference.setIsDairyFree(request.getIsDairyFree());
        if (request.getIsNutFree() != null)
            preference.setIsNutFree(request.getIsNutFree());

        if (request.getAllergies() != null)
            preference.setAllergies(request.getAllergies());
        if (request.getDislikes() != null)
            preference.setDislikes(request.getDislikes());
        if (request.getSpecialInstructions() != null)
            preference.setSpecialInstructions(request.getSpecialInstructions());
        if (request.getPortionPreference() != null)
            preference.setPortionPreference(request.getPortionPreference());
        if (request.getVisibleToChefs() != null)
            preference.setVisibleToChefs(request.getVisibleToChefs());

        CustomerPreference saved = preferenceRepository.save(preference);
        log.info("Preferences updated for customer {}", currentUser.getId());

        return mapToPreferenceResponse(saved);
    }

    @Transactional(readOnly = true)
    public CustomerPreferenceResponse getMyPreferences(UserPrincipal currentUser) {
        CustomerPreference preference = preferenceRepository
                .findByCustomerId(currentUser.getId())
                .orElse(new CustomerPreference());

        if (preference.getId() == null) {
            preference.setCustomerId(currentUser.getId());
        }

        return mapToPreferenceResponse(preference);
    }

    @Transactional(readOnly = true)
    public PreferenceSummaryResponse getPreferenceSummary(UserPrincipal currentUser) {
        PreferenceSummaryResponse summary = new PreferenceSummaryResponse();
        summary.setCustomerId(currentUser.getId());

        CustomerPreference preference = preferenceRepository
                .findByCustomerId(currentUser.getId())
                .orElse(null);

        summary.setHasGlobalPreferences(preference != null && preference.getId() != null);
        summary.setHasDietaryRestrictions(preference != null && preference.hasDietaryRestrictions());
        summary.setHasAllergies(preference != null && preference.hasAllergies());
        summary.setFavoriteCount(favoriteRepository.countByCustomerId(currentUser.getId()));
        summary.setItemPreferenceCount((long) menuItemPreferenceRepository
                .findByCustomerId(currentUser.getId()).size());

        if (preference != null) {
            summary.setPreferences(mapToPreferenceResponse(preference));
        }

        return summary;
    }

    @Transactional
    public FavoriteMenuItemResponse addFavorite(AddFavoriteRequest request,
                                                UserPrincipal currentUser) {
        log.info("Adding favorite for customer {}: menu item {}",
                currentUser.getId(), request.getMenuItemId());

        if (!currentUser.hasRole("CUSTOMER")) {
            throw new ForbiddenException("Only customers can add favorites");
        }

        // Check if already favorite
        if (favoriteRepository.existsByCustomerIdAndMenuItemId(
                currentUser.getId(), request.getMenuItemId())) {
            throw new BadRequestException("Menu item is already in favorites");
        }

        // Verify menu item exists
        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        FavoriteMenuItem favorite = new FavoriteMenuItem();
        favorite.setCustomerId(currentUser.getId());
        favorite.setMenuItemId(request.getMenuItemId());
        favorite.setRestaurantId(menuItem.getRestaurantId());
        favorite.setNotes(request.getNotes());

        FavoriteMenuItem saved = favoriteRepository.save(favorite);
        log.info("Favorite added successfully");

        return mapToFavoriteResponse(saved);
    }

    @Transactional
    public void removeFavorite(Long menuItemId, UserPrincipal currentUser) {
        log.info("Removing favorite for customer {}: menu item {}",
                currentUser.getId(), menuItemId);

        if (!favoriteRepository.existsByCustomerIdAndMenuItemId(
                currentUser.getId(), menuItemId)) {
            throw new ResourceNotFoundException("Favorite not found");
        }

        favoriteRepository.deleteByCustomerIdAndMenuItemId(currentUser.getId(), menuItemId);
        log.info("Favorite removed successfully");
    }

    @Transactional(readOnly = true)
    public List<FavoriteMenuItemResponse> getMyFavorites(UserPrincipal currentUser) {
        List<FavoriteMenuItem> favorites = favoriteRepository
                .findByCustomerId(currentUser.getId());

        return favorites.stream()
                .map(this::mapToFavoriteResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FavoriteMenuItemResponse> getMyFavoritesByRestaurant(Long restaurantId,
                                                                     UserPrincipal currentUser) {
        List<FavoriteMenuItem> favorites = favoriteRepository
                .findByCustomerIdAndRestaurantId(currentUser.getId(), restaurantId);

        return favorites.stream()
                .map(this::mapToFavoriteResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void incrementFavoriteOrderCount(Long customerId, Long menuItemId) {
        favoriteRepository.findByCustomerIdAndMenuItemId(customerId, menuItemId)
                .ifPresent(favorite -> {
                    favorite.incrementOrderCount();
                    favoriteRepository.save(favorite);
                });
    }

    @Transactional
    public MenuItemPreferenceResponse updateMenuItemPreference(
            UpdateMenuItemPreferenceRequest request,
            UserPrincipal currentUser) {
        log.info("Updating menu item preference for customer {}: item {}",
                currentUser.getId(), request.getMenuItemId());

        if (!currentUser.hasRole("CUSTOMER")) {
            throw new ForbiddenException("Only customers can manage preferences");
        }

        MenuItemPreference preference = menuItemPreferenceRepository
                .findByCustomerIdAndMenuItemId(currentUser.getId(), request.getMenuItemId())
                .orElse(new MenuItemPreference());

        preference.setCustomerId(currentUser.getId());
        preference.setMenuItemId(request.getMenuItemId());

        if (request.getSpiceLevel() != null)
            preference.setSpiceLevel(request.getSpiceLevel());
        if (request.getCookingPreference() != null)
            preference.setCookingPreference(request.getCookingPreference());
        if (request.getExtraIngredients() != null)
            preference.setExtraIngredients(request.getExtraIngredients());
        if (request.getRemoveIngredients() != null)
            preference.setRemoveIngredients(request.getRemoveIngredients());
        if (request.getSpecialInstructions() != null)
            preference.setSpecialInstructions(request.getSpecialInstructions());

        MenuItemPreference saved = menuItemPreferenceRepository.save(preference);
        log.info("Menu item preference updated successfully");

        return mapToMenuItemPreferenceResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MenuItemPreferenceResponse> getMyMenuItemPreferences(UserPrincipal currentUser) {
        List<MenuItemPreference> preferences = menuItemPreferenceRepository
                .findByCustomerId(currentUser.getId());

        return preferences.stream()
                .map(this::mapToMenuItemPreferenceResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteMenuItemPreference(Long menuItemId, UserPrincipal currentUser) {
        log.info("Deleting menu item preference for customer {}: item {}",
                currentUser.getId(), menuItemId);

        if (!menuItemPreferenceRepository.existsByCustomerIdAndMenuItemId(
                currentUser.getId(), menuItemId)) {
            throw new ResourceNotFoundException("Menu item preference not found");
        }

        menuItemPreferenceRepository.deleteByCustomerIdAndMenuItemId(
                currentUser.getId(), menuItemId);
        log.info("Menu item preference deleted successfully");
    }

    // Chef-specific methods
    @Transactional(readOnly = true)
    public ChefViewCustomerPreferenceResponse getCustomerPreferencesForOrder(
            Long customerId, Long menuItemId, UserPrincipal currentUser) {

        if (!currentUser.hasAnyRole("CHEF", "RESTAURANT_ADMIN", "ADMIN")) {
            throw new ForbiddenException("Only chefs can view customer preferences");
        }

        CustomerPreference globalPref = preferenceRepository
                .findByCustomerId(customerId)
                .orElse(null);

        if (globalPref == null || !globalPref.getVisibleToChefs()) {
            return null; // Customer has hidden preferences
        }

        ChefViewCustomerPreferenceResponse response = new ChefViewCustomerPreferenceResponse();
        response.setCustomerId(customerId);
        response.setCustomerName("Customer #" + customerId); // Anonymized

        // Global preferences
        response.setSpiceLevel(globalPref.getSpiceLevel());
        response.setSweetnessLevel(globalPref.getSweetnessLevel());
        response.setSaltLevel(globalPref.getSaltLevel());
        response.setCookingPreference(globalPref.getCookingPreference());
        response.setTemperaturePreference(globalPref.getTemperaturePreference());
        response.setIsVegetarian(globalPref.getIsVegetarian());
        response.setIsVegan(globalPref.getIsVegan());
        response.setIsGlutenFree(globalPref.getIsGlutenFree());
        response.setIsDairyFree(globalPref.getIsDairyFree());
        response.setIsNutFree(globalPref.getIsNutFree());
        response.setAllergies(globalPref.getAllergies());
        response.setDislikes(globalPref.getDislikes());
        response.setSpecialInstructions(globalPref.getSpecialInstructions());
        response.setPortionPreference(globalPref.getPortionPreference());

        // Menu item specific preference
        if (menuItemId != null) {
            MenuItemPreference itemPref = menuItemPreferenceRepository
                    .findByCustomerIdAndMenuItemId(customerId, menuItemId)
                    .orElse(null);

            if (itemPref != null) {
                response.setMenuItemPreference(mapToMenuItemPreferenceResponse(itemPref));
            }
        }

        return response;
    }

    @Transactional(readOnly = true)
    public OrderPreferencesSummary getOrderPreferences(Order order, UserPrincipal currentUser) {

        if (!currentUser.hasAnyRole("CHEF", "RESTAURANT_ADMIN", "ADMIN")) {
            throw new ForbiddenException("Only chefs can view order preferences");
        }

        OrderPreferencesSummary summary = new OrderPreferencesSummary();
        summary.setOrderId(order.getId());
        summary.setOrderNumber(order.getOrderNumber());
        summary.setCustomerId(order.getCustomerId());

        User customer = userRepository.findById(order.getCustomerId()).orElse(null);
        summary.setCustomerName(customer != null ? customer.getFullName() : "Customer");

        // Global preferences
        CustomerPreference globalPref = preferenceRepository
                .findByCustomerId(order.getCustomerId())
                .orElse(null);

        if (globalPref != null && globalPref.getVisibleToChefs()) {
            summary.setGlobalPreferences(mapToPreferenceResponse(globalPref));

            // Allergy warnings
            if (globalPref.hasAllergies()) {
                summary.setAllergyWarnings(globalPref.getAllergies());
            }

            // Dietary restrictions
            List<String> restrictions = new ArrayList<>();
            if (globalPref.getIsVegetarian()) restrictions.add("Vegetarian");
            if (globalPref.getIsVegan()) restrictions.add("Vegan");
            if (globalPref.getIsGlutenFree()) restrictions.add("Gluten-Free");
            if (globalPref.getIsDairyFree()) restrictions.add("Dairy-Free");
            if (globalPref.getIsNutFree()) restrictions.add("Nut-Free");
            summary.setDietaryRestrictions(restrictions);
        }

        // Item-specific preferences
        List<Long> menuItemIds = order.getOrderItems().stream()
                .map(OrderItem::getMenuItemId)
                .collect(Collectors.toList());

        List<MenuItemPreference> itemPrefs = menuItemPreferenceRepository
                .findByCustomerIdAndMenuItemIdIn(order.getCustomerId(), menuItemIds);

        summary.setItemPreferences(itemPrefs.stream()
                .map(this::mapToMenuItemPreferenceResponse)
                .collect(Collectors.toList()));

        // Aggregate special instructions
        List<String> allInstructions = new ArrayList<>();
        if (globalPref != null && globalPref.getSpecialInstructions() != null) {
            allInstructions.add(globalPref.getSpecialInstructions());
        }
        itemPrefs.stream()
                .filter(p -> p.getSpecialInstructions() != null)
                .forEach(p -> allInstructions.add(p.getSpecialInstructions()));
        summary.setSpecialInstructions(allInstructions);

        return summary;
    }

    private CustomerPreferenceResponse mapToPreferenceResponse(CustomerPreference preference) {
        CustomerPreferenceResponse response = new CustomerPreferenceResponse();
        response.setId(preference.getId());
        response.setCustomerId(preference.getCustomerId());
        response.setSpiceLevel(preference.getSpiceLevel());
        response.setSweetnessLevel(preference.getSweetnessLevel());
        response.setSaltLevel(preference.getSaltLevel());
        response.setCookingPreference(preference.getCookingPreference());
        response.setTemperaturePreference(preference.getTemperaturePreference());
        response.setIsVegetarian(preference.getIsVegetarian());
        response.setIsVegan(preference.getIsVegan());
        response.setIsGlutenFree(preference.getIsGlutenFree());
        response.setIsDairyFree(preference.getIsDairyFree());
        response.setIsNutFree(preference.getIsNutFree());
        response.setAllergies(preference.getAllergies());
        response.setDislikes(preference.getDislikes());
        response.setSpecialInstructions(preference.getSpecialInstructions());
        response.setPortionPreference(preference.getPortionPreference());
        response.setVisibleToChefs(preference.getVisibleToChefs());
        response.setCreatedAt(preference.getCreatedAt());
        response.setUpdatedAt(preference.getUpdatedAt());
        return response;
    }

    private FavoriteMenuItemResponse mapToFavoriteResponse(FavoriteMenuItem favorite) {
        FavoriteMenuItemResponse response = new FavoriteMenuItemResponse();
        response.setId(favorite.getId());
        response.setCustomerId(favorite.getCustomerId());
        response.setMenuItemId(favorite.getMenuItemId());
        response.setRestaurantId(favorite.getRestaurantId());
        response.setNotes(favorite.getNotes());
        response.setOrderCount(favorite.getOrderCount());
        response.setLastOrderedAt(favorite.getLastOrderedAt());
        response.setCreatedAt(favorite.getCreatedAt());

        // Fetch menu item name
        menuItemRepository.findById(favorite.getMenuItemId())
                .ifPresent(item -> response.setMenuItemName(item.getName()));

        return response;
    }

    private MenuItemPreferenceResponse mapToMenuItemPreferenceResponse(MenuItemPreference preference) {
        MenuItemPreferenceResponse response = new MenuItemPreferenceResponse();
        response.setId(preference.getId());
        response.setCustomerId(preference.getCustomerId());
        response.setMenuItemId(preference.getMenuItemId());
        response.setSpiceLevel(preference.getSpiceLevel());
        response.setCookingPreference(preference.getCookingPreference());
        response.setExtraIngredients(preference.getExtraIngredients());
        response.setRemoveIngredients(preference.getRemoveIngredients());
        response.setSpecialInstructions(preference.getSpecialInstructions());
        response.setCreatedAt(preference.getCreatedAt());
        response.setUpdatedAt(preference.getUpdatedAt());

        // Fetch menu item name
        menuItemRepository.findById(preference.getMenuItemId())
                .ifPresent(item -> response.setMenuItemName(item.getName()));

        return response;
    }
}
