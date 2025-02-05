package com.example.user_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private static final String EMAIL_QUEUE = "welcomeEmailQueue";
    private static final String EMAIL_EXCHANGE = "welcomeEmailExchange";
    private static final String EMAIL_ROUTING_KEY = "welcome.email";

    private static final String VERIFICATION_EMAIL_QUEUE = "verificationEmailQueue";
    private static final String VERIFICATION_EMAIL_EXCHANGE = "verificationEmailExchange";
    private static final String VERIFICATION_EMAIL_ROUTING_KEY = "verification.email";

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Welcome email
    @Bean
    public Queue welcomeEmailQueue() {
        return new Queue(EMAIL_QUEUE, false);
    }

    @Bean
    public TopicExchange welcomeEmailExchange() {
        return new TopicExchange(EMAIL_EXCHANGE);
    }

    @Bean
    public Binding welcomeEmailBinding(Queue welcomeEmailQueue, TopicExchange welcomeEmailExchange) {
        return BindingBuilder.bind(welcomeEmailQueue).to(welcomeEmailExchange).with(EMAIL_ROUTING_KEY);
    }


    // Verification email
    @Bean
    public Queue verificationEmailQueue() {
        return new Queue(VERIFICATION_EMAIL_QUEUE, false);
    }

    @Bean
    public TopicExchange verificationEmailExchange() {
        return new TopicExchange(VERIFICATION_EMAIL_EXCHANGE);
    }

    @Bean
    public Binding verificationEmailBinding(Queue verificationEmailQueue, TopicExchange verificationEmailExchange) {
        return BindingBuilder.bind(verificationEmailQueue).to(verificationEmailExchange).with(VERIFICATION_EMAIL_ROUTING_KEY);
    }
}
