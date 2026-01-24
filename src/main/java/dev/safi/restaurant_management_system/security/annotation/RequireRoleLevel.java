package dev.safi.restaurant_management_system.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for role-level restrictions
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRoleLevel {

    /**
     * Minimum role level required
     */
    int value();

    /**
     * Whether to log access attempts
     */
    boolean audit() default true;
}
