package com.truck.utils.gateway.utils.annotation;

import java.lang.annotation.*;

/**
 * Created by truck on 2015/11/30.
 */

/**
 * The class to which this annotation is applied is not thread-safe.
 * This annotation primarily exists for clarifying the non-thread-safety of a class
 * that might otherwise be assumed to be thread-safe, despite the fact that it is a bad
 * idea to assume a class is thread-safe without good reason.
 *
 * @see ThreadSafe
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS) // The original version used RUNTIME
public @interface NotThreadSafe {}
