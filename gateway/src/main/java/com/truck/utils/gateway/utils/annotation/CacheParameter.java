package com.truck.utils.gateway.utils.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by truck on 15/11/28.
 */

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheParameter {
    CacheKeyType type() default CacheKeyType.Normal;

    public static enum CacheKeyType {
        Normal,
        PageIndex,
        PageSize,
        Filter
    }
}

