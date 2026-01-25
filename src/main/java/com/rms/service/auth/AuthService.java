package com.rms.service.auth;

import com.rms.dto.auth.*;
import dev.safi.restaurant_management_system.dto.auth.*;
import com.rms.entity.RefreshToken;
import com.rms.entity.Restaurant;
import com.rms.entity.Role;
import com.rms.entity.User;
import com.rms.exception.BadRequestException;
import com.rms.exception.ResourceNotFoundException;
import com.rms.exception.TokenRefreshException;
import com.rms.repository.RefreshTokenRepository;
import com.rms.repository.RestaurantRepository;
import com.rms.repository.RoleRepository;
import com.rms.repository.UserRepository;
import com.rms.security.JwtTokenProvider;
import com.rms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Authentication Service - Handles all authentication logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RestaurantRepository restaurantRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    /**
     * User login with JWT generation
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String accessToken = tokenProvider.generateAccessToken(authentication);

        // Revoke old refresh tokens and create new one
        refreshTokenRepository.revokeAllUserTokens(userPrincipal.getId());
        RefreshToken refreshToken = createRefreshToken(userPrincipal.getId());

        // Update last login
        userRepository.updateLastLogin(userPrincipal.getId(), LocalDateTime.now());

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        log.info("User logged in successfully: {}", request.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .user(mapToUserInfo(user))
                .build();
    }

    /**
     * Customer registration
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        if (request.getPhoneNumber() != null &&
                userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("Phone number is already registered");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .restaurantId(request.getRestaurantId())
                .isActive(true)
                .isEmailVerified(false)
                .isPhoneVerified(false)
                .build();

        // Assign CUSTOMER role by default
        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Customer role not found"));
        user.setRoles(Set.of(customerRole));

        user = userRepository.save(user);

        log.info("New customer registered: {}", request.getEmail());

        // Auto-login after registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String accessToken = tokenProvider.generateAccessToken(authentication);
        RefreshToken refreshToken = createRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .user(mapToUserInfo(user))
                .build();
    }

    /**
     * Restaurant onboarding (used by Salesman)
     */
    @Transactional
    public ApiResponse<RestaurantInfo> registerRestaurant(RestaurantRegisterRequest request) {
        if (restaurantRepository.existsByEmail(request.getRestaurantEmail())) {
            throw new BadRequestException("Restaurant email is already registered");
        }

        if (userRepository.existsByEmail(request.getAdminEmail())) {
            throw new BadRequestException("Admin email is already registered");
        }

        // Generate unique restaurant code
        String restaurantCode = generateRestaurantCode();

        // Create restaurant
        Restaurant restaurant = Restaurant.builder()
                .restaurantCode(restaurantCode)
                .name(request.getRestaurantName())
                .email(request.getRestaurantEmail())
                .phoneNumber(request.getRestaurantPhone())
                .address(request.getAddress())
                .subscriptionStatus(SubscriptionStatus.TRIAL)
                .subscriptionExpiry(LocalDateTime.now().plusDays(30)) // 30-day trial
                .isActive(true)
                .build();

        restaurant = restaurantRepository.save(restaurant);

        // Create restaurant admin user
        User admin = User.builder()
                .fullName(request.getAdminName())
                .email(request.getAdminEmail())
                .password(passwordEncoder.encode(request.getAdminPassword()))
                .restaurantId(restaurant.getId())
                .isActive(true)
                .isEmailVerified(false)
                .isPhoneVerified(false)
                .build();

        Role adminRole = roleRepository.findByName("ROLE_RESTAURANT_ADMIN")
                .orElseThrow(() -> new RuntimeException("Admin role not found"));
        admin.setRoles(Set.of(adminRole));

        userRepository.save(admin);

        log.info("New restaurant registered: {} (Code: {})", restaurant.getName(), restaurantCode);

        RestaurantInfo info = new RestaurantInfo();
        info.setRestaurantId(restaurant.getId());
        info.setRestaurantCode(restaurantCode);
        info.setRestaurantName(restaurant.getName());
        info.setAdminEmail(admin.getEmail());
        info.setSubscriptionStatus(restaurant.getSubscriptionStatus().name());
        info.setSubscriptionExpiry(restaurant.getSubscriptionExpiry());

        return ApiResponse.success("Restaurant registered successfully", info);
    }

    /**
     * Refresh access token using refresh token
     */
    @Transactional
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndIsRevokedFalse(requestRefreshToken)
                .orElseThrow(() -> new TokenRefreshException("Invalid or revoked refresh token"));

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException("Refresh token expired. Please login again");
        }

        User user = refreshToken.getUser();
        UserPrincipal userPrincipal = UserPrincipal.create(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities()
        );

        String newAccessToken = tokenProvider.generateAccessToken(authentication);

        // Optionally rotate refresh token
        refreshTokenRepository.revokeToken(requestRefreshToken);
        RefreshToken newRefreshToken = createRefreshToken(user.getId());

        log.info("Token refreshed for user: {}", user.getEmail());

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .build();
    }

    /**
     * Logout - revoke refresh token
     */
    @Transactional
    public ApiResponse<Void> logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenRepository.revokeToken(refreshToken);
        }
        SecurityContextHolder.clearContext();

        log.info("User logged out successfully");

        return ApiResponse.success("Logged out successfully", null);
    }

    /**
     * Helper: Create refresh token
     */
    private RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String tokenValue = tokenProvider.generateRefreshToken(userId);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiryDate(LocalDateTime.now().plusMillis(tokenProvider.getRefreshTokenExpirationMs()))
                .isRevoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Helper: Generate unique restaurant code
     */
    private String generateRestaurantCode() {
        String code;
        do {
            code = "REST" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (restaurantRepository.existsByRestaurantCode(code));
        return code;
    }

    /**
     * Helper: Map User entity to UserInfo DTO
     */
    private UserInfo mapToUserInfo(User user) {
        Restaurant restaurant = null;
        if (user.getRestaurantId() != null) {
            restaurant = restaurantRepository.findById(user.getRestaurantId()).orElse(null);
        }

        return UserInfo.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .restaurantId(user.getRestaurantId())
                .restaurantName(restaurant != null ? restaurant.getName() : null)
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .isEmailVerified(user.getIsEmailVerified())
                .isPhoneVerified(user.getIsPhoneVerified())
                .lastLogin(user.getLastLogin())
                .build();
    }
}

