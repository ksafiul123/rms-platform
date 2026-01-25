package com.rms.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for multiple permissions (any of them grants access)
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAnyPermission {

    /**
     * List of permissions (user needs ANY one)
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
