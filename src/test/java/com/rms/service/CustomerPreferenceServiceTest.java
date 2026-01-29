package com.rms.service;

import static org.junit.jupiter.api.Assertions.*;

//package com.rms.service;

import com.rms.dto.PreferenceDTO.*;
import com.rms.entity.*;
import com.rms.exception.BadRequestException;
import com.rms.exception.ForbiddenException;
import com.rms.exception.ResourceNotFoundException;
import com.rms.repository.*;
import com.rms.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerPreferenceServiceTest {

    @Mock
    private CustomerPreferenceRepository preferenceRepository;

    @Mock
    private FavoriteMenuItemRepository favoriteRepository;

    @Mock
    private MenuItemPreferenceRepository menuItemPreferenceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private CustomerPreferenceService preferenceService;

    private UserPrincipal customerPrincipal;
    private UserPrincipal chefPrincipal;

    @BeforeEach
    void setUp() {
        customerPrincipal = createUserPrincipal(1L, 100L, List.of("CUSTOMER"));
        chefPrincipal = createUserPrincipal(2L, 100L, List.of("CHEF"));
    }

    @Test
    void updatePreferences_Success() {
        // Arrange
        UpdateCustomerPreferenceRequest request = new UpdateCustomerPreferenceRequest();
        request.setSpiceLevel(CustomerPreference.SpiceLevel.HOT);
        request.setIsVegetarian(true);
        request.setAllergies(List.of("Peanuts", "Shellfish"));
        request.setVisibleToChefs(true);

        CustomerPreference existing = new CustomerPreference();
        existing.setCustomerId(1L);

        when(preferenceRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(existing));
        when(preferenceRepository.save(any(CustomerPreference.class)))
                .thenAnswer(i -> {
                    CustomerPreference pref = i.getArgument(0);
                    pref.setId(1L);
                    return pref;
                });

        // Act
        CustomerPreferenceResponse response = preferenceService.updatePreferences(
                request, customerPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals(CustomerPreference.SpiceLevel.HOT, response.getSpiceLevel());
        assertTrue(response.getIsVegetarian());
        assertEquals(2, response.getAllergies().size());
        verify(preferenceRepository).save(any(CustomerPreference.class));
    }

    @Test
    void updatePreferences_NonCustomer_ThrowsForbidden() {
        // Arrange
        UpdateCustomerPreferenceRequest request = new UpdateCustomerPreferenceRequest();

        // Act & Assert
        assertThrows(ForbiddenException.class,
                () -> preferenceService.updatePreferences(request, chefPrincipal));
    }

    @Test
    void addFavorite_Success() {
        // Arrange
        AddFavoriteRequest request = new AddFavoriteRequest();
        request.setMenuItemId(10L);
        request.setNotes("My favorite pizza");

        MenuItem menuItem = new MenuItem();
        menuItem.setId(10L);
        menuItem.setRestaurantId(100L);
        menuItem.setName("Margherita Pizza");

        when(favoriteRepository.existsByCustomerIdAndMenuItemId(1L, 10L))
                .thenReturn(false);
        when(menuItemRepository.findById(10L))
                .thenReturn(Optional.of(menuItem));
        when(favoriteRepository.save(any(FavoriteMenuItem.class)))
                .thenAnswer(i -> {
                    FavoriteMenuItem fav = i.getArgument(0);
                    fav.setId(1L);
                    return fav;
                });

        // Act
        FavoriteMenuItemResponse response = preferenceService.addFavorite(
                request, customerPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals(10L, response.getMenuItemId());
        assertEquals("My favorite pizza", response.getNotes());
        verify(favoriteRepository).save(any(FavoriteMenuItem.class));
    }

    @Test
    void addFavorite_AlreadyExists_ThrowsBadRequest() {
        // Arrange
        AddFavoriteRequest request = new AddFavoriteRequest();
        request.setMenuItemId(10L);

        when(favoriteRepository.existsByCustomerIdAndMenuItemId(1L, 10L))
                .thenReturn(true);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> preferenceService.addFavorite(request, customerPrincipal));
    }

    @Test
    void addFavorite_MenuItemNotFound_ThrowsNotFound() {
        // Arrange
        AddFavoriteRequest request = new AddFavoriteRequest();
        request.setMenuItemId(999L);

        when(favoriteRepository.existsByCustomerIdAndMenuItemId(1L, 999L))
                .thenReturn(false);
        when(menuItemRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> preferenceService.addFavorite(request, customerPrincipal));
    }

    @Test
    void removeFavorite_Success() {
        // Arrange
        when(favoriteRepository.existsByCustomerIdAndMenuItemId(1L, 10L))
                .thenReturn(true);
        doNothing().when(favoriteRepository)
                .deleteByCustomerIdAndMenuItemId(1L, 10L);

        // Act
        preferenceService.removeFavorite(10L, customerPrincipal);

        // Assert
        verify(favoriteRepository).deleteByCustomerIdAndMenuItemId(1L, 10L);
    }

    @Test
    void removeFavorite_NotFound_ThrowsNotFound() {
        // Arrange
        when(favoriteRepository.existsByCustomerIdAndMenuItemId(1L, 999L))
                .thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> preferenceService.removeFavorite(999L, customerPrincipal));
    }

    @Test
    void updateMenuItemPreference_Success() {
        // Arrange
        UpdateMenuItemPreferenceRequest request = new UpdateMenuItemPreferenceRequest();
        request.setMenuItemId(10L);
        request.setSpiceLevel(CustomerPreference.SpiceLevel.EXTRA_HOT);
        request.setExtraIngredients("extra cheese, extra sauce");
        request.setRemoveIngredients("onions");

        MenuItemPreference existing = new MenuItemPreference();
        existing.setCustomerId(1L);
        existing.setMenuItemId(10L);

        when(menuItemPreferenceRepository.findByCustomerIdAndMenuItemId(1L, 10L))
                .thenReturn(Optional.of(existing));
        when(menuItemPreferenceRepository.save(any(MenuItemPreference.class)))
                .thenAnswer(i -> {
                    MenuItemPreference pref = i.getArgument(0);
                    pref.setId(1L);
                    return pref;
                });

        // Act
        MenuItemPreferenceResponse response = preferenceService.updateMenuItemPreference(
                request, customerPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals(CustomerPreference.SpiceLevel.EXTRA_HOT, response.getSpiceLevel());
        assertEquals("extra cheese, extra sauce", response.getExtraIngredients());
        verify(menuItemPreferenceRepository).save(any(MenuItemPreference.class));
    }

    @Test
    void getCustomerPreferencesForOrder_VisibleToChefs_ReturnsPreferences() {
        // Arrange
        CustomerPreference preference = new CustomerPreference();
        preference.setCustomerId(1L);
        preference.setSpiceLevel(CustomerPreference.SpiceLevel.HOT);
        preference.setIsVegetarian(true);
        preference.setAllergies(List.of("Peanuts"));
        preference.setVisibleToChefs(true);

        when(preferenceRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(preference));

        // Act
        ChefViewCustomerPreferenceResponse response = preferenceService
                .getCustomerPreferencesForOrder(1L, null, chefPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getCustomerId());
        assertEquals(CustomerPreference.SpiceLevel.HOT, response.getSpiceLevel());
        assertTrue(response.getIsVegetarian());
        assertEquals(1, response.getAllergies().size());
    }

    @Test
    void getCustomerPreferencesForOrder_HiddenFromChefs_ReturnsNull() {
        // Arrange
        CustomerPreference preference = new CustomerPreference();
        preference.setCustomerId(1L);
        preference.setVisibleToChefs(false);

        when(preferenceRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(preference));

        // Act
        ChefViewCustomerPreferenceResponse response = preferenceService
                .getCustomerPreferencesForOrder(1L, null, chefPrincipal);

        // Assert
        assertNull(response);
    }

    @Test
    void getCustomerPreferencesForOrder_NonChef_ThrowsForbidden() {
        // Arrange
        // Act & Assert
        assertThrows(ForbiddenException.class,
                () -> preferenceService.getCustomerPreferencesForOrder(
                        1L, null, customerPrincipal));
    }

    @Test
    void incrementFavoriteOrderCount_Success() {
        // Arrange
        FavoriteMenuItem favorite = new FavoriteMenuItem();
        favorite.setId(1L);
        favorite.setCustomerId(1L);
        favorite.setMenuItemId(10L);
        favorite.setOrderCount(5);

        when(favoriteRepository.findByCustomerIdAndMenuItemId(1L, 10L))
                .thenReturn(Optional.of(favorite));
        when(favoriteRepository.save(any(FavoriteMenuItem.class)))
                .thenReturn(favorite);

        // Act
        preferenceService.incrementFavoriteOrderCount(1L, 10L);

        // Assert
        verify(favoriteRepository).save(argThat(f ->
                f.getOrderCount() == 6 && f.getLastOrderedAt() != null
        ));
    }

    @Test
    void getPreferenceSummary_WithPreferences_ReturnsFullSummary() {
        // Arrange
        CustomerPreference preference = new CustomerPreference();
        preference.setId(1L);
        preference.setCustomerId(1L);
        preference.setIsVegetarian(true);
        preference.setAllergies(List.of("Peanuts"));

        when(preferenceRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(preference));
        when(favoriteRepository.countByCustomerId(1L))
                .thenReturn(5L);
        when(menuItemPreferenceRepository.findByCustomerId(1L))
                .thenReturn(List.of(new MenuItemPreference(), new MenuItemPreference()));

        // Act
        PreferenceSummaryResponse summary = preferenceService
                .getPreferenceSummary(customerPrincipal);

        // Assert
        assertNotNull(summary);
        assertTrue(summary.getHasGlobalPreferences());
        assertTrue(summary.getHasDietaryRestrictions());
        assertTrue(summary.getHasAllergies());
        assertEquals(5L, summary.getFavoriteCount());
        assertEquals(2L, summary.getItemPreferenceCount());
    }

    // Helper methods
    private UserPrincipal createUserPrincipal(Long userId, Long restaurantId, List<String> roles) {
        return UserPrincipal.builder()
                .id(userId)
                .email("user" + userId + "@test.com")
                .restaurantId(restaurantId)
                .roles(roles)
                .build();
    }
}