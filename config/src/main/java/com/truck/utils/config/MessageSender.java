package com.truck.utils.config;

import org.springframework.jms.core.JmsTemplate;

import java.util.Map;

/**
 * Created by Rocky on 16/3/2.
 */
public class MessageSender {

    private JmsTemplate jmsTemplate;
    private String queue;

    public MessageSender(JmsTemplate jmsTemplate,String queue){
        this.jmsTemplate = jmsTemplate;
        this.queue = queue;
    }

    public void sendMessage(Map<String,Object> message){
        jmsTemplate.convertAndSend(queue,message);
    }
}
