package com.rms.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for multiple permissions (all required)
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAllPermissions {

    /**
     * List of permissions (user needs ALL)
     */
    String[] value();

    /**
     * Resource being accessed
     */
    String resource() default "";

    /**
     * Whether to log access attempts
     */
    boolean audit() default true;
}
