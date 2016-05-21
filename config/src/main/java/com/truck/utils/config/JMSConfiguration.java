package com.truck.utils.config;

import org.springframework.context.annotation.Bean;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.SimpleJmsListenerContainerFactory;

import javax.jms.ConnectionFactory;

/**
 * Created by Rocky on 16/3/2.
 */
public class JMSConfiguration {

    @Bean
    JmsListenerContainerFactory<?> containerFactory(ConnectionFactory connectionFactory) {
        SimpleJmsListenerContainerFactory factory = new SimpleJmsListenerContainerFactory();
        factory.setSessionTransacted(true);
        factory.setConnectionFactory(connectionFactory);
        return factory;
    }
}
