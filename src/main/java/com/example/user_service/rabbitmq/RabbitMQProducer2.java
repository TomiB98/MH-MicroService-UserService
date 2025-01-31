package com.example.user_service.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer2 {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private TopicExchange welcomeEmailExchange;


    public void sendWelcomeEmail(String email) {
        rabbitTemplate.convertAndSend(welcomeEmailExchange.getName(), "welcome.email", email);
    }
}
