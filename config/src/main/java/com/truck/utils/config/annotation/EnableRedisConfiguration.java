package com.truck.utils.config.annotation;

import com.truck.utils.config.CacheConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Rocky on 16/2/24.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({CacheConfiguration.class})
public @interface EnableRedisConfiguration {

    String[] masterNames() default {"cache"};
}
