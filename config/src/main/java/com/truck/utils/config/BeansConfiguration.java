package com.truck.utils.config;

import org.dozer.DozerBeanMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Rocky on 15/12/25.
 */
@Configuration
public class BeansConfiguration {

    @Bean
    public DozerBeanMapper dozerMapper(){
        return new DozerBeanMapper();
    }
}
