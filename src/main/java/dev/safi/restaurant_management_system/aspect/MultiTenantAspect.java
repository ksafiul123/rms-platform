package dev.safi.restaurant_management_system.aspect;

/**
 * Multi-Tenant Aspect - Automatically inject restaurantId
 */
//package com.rms.aspect;

import dev.safi.restaurant_management_system.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class MultiTenantAspect {

    /**
     * Automatically inject restaurantId for tenant isolation
     * This can be used to validate access to resources
     */
    @Around("@annotation(com.rms.annotation.RequireRestaurantAccess)")
    public Object validateRestaurantAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        // Extract restaurantId from method parameters
        Object[] args = joinPoint.getArgs();
        Long requestedRestaurantId = null;

        for (Object arg : args) {
            if (arg instanceof Long) {
                requestedRestaurantId = (Long) arg;
                break;
            }
        }

        // Validate access
        if (requestedRestaurantId != null && principal.getRestaurantId() != null) {
            if (!requestedRestaurantId.equals(principal.getRestaurantId())) {
                boolean isSuperAdmin = principal.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_SUPER_ADMIN"));

                if (!isSuperAdmin) {
                    log.warn("Access denied: User {} attempted to access restaurant {}",
                            principal.getId(), requestedRestaurantId);
                    throw new SecurityException("Access denied to this restaurant");
                }
            }
        }

        return joinPoint.proceed();
    }
}

