package com.example.user_service.rabbitmq;

import com.example.user_service.dtos.VerificationEmailDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer2 {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    @Qualifier("welcomeEmailExchange")
    private TopicExchange welcomeEmailExchange;

    @Autowired
    @Qualifier("verificationEmailExchange")
    private TopicExchange verificationEmailExchange;


    public void sendWelcomeEmail(String email) {
        rabbitTemplate.convertAndSend(welcomeEmailExchange.getName(), "welcome.email", email);
    }

    public void sendVerificationEmail(VerificationEmailDTO verificationEmailDTO) {
        rabbitTemplate.convertAndSend(verificationEmailExchange.getName(), "verification.email", verificationEmailDTO);
    }
}
