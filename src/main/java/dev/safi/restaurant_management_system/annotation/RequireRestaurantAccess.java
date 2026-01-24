package dev.safi.restaurant_management_system.annotation;

/**
 * Custom annotation for restaurant access validation
 */
//package com.rms.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRestaurantAccess {
}


