package com.truck.utils.gateway.utils.annotation;

import com.truck.utils.gateway.utils.define.MockApiReturnObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by truck on 15/11/28.
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiShortCircuit {
    Class<? extends MockApiReturnObject> value();
}
