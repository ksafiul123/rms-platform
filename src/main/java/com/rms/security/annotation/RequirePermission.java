package com.rms.security.annotation;

//package com.rms.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for permission-based access control
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * Required permission name (e.g., "menu:create")
     */
    String value();

    /**
     * Resource being accessed (optional, for logging)
     */
    String resource() default "";

    /**
     * Whether to log access attempts
     */
    boolean audit() default true;
}

///**
// * Permission Check Aspect
// */
//package com.rms.security.aspect;
//
//import com.rms.entity.AuditAction;
//import com.rms.entity.PermissionAuditLog;
//import com.rms.exception.InsufficientPermissionException;
//import com.rms.repository.PermissionAuditLogRepository;
//import com.rms.security.UserPrincipal;
//import com.rms.security.annotation.*;
//        import com.rms.service.rbac.PermissionService;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import java.lang.reflect.Method;
//import java.time.LocalDateTime;
//
///**
// * Method Security Expression Handler
// */
//package com.rms.security;
//
//import com.rms.service.rbac.PermissionService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.access.expression.SecurityExpressionRoot;
//import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Component;
//
///**
// * Security Utility Class
// */
//package com.rms.security;
//
//import org.springframework.security.core.context.SecurityContextHolder;
//
