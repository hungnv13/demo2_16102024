package vn.vnpay.demo2_16102024.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String QUEUE_NAME = "paymentQueue";
    public static final String EXCHANGE_NAME = "paymentExchange";
    public static final String ROUTING_KEY = "paymentRoutingKey";

    @Bean
    public Queue paymentQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }
}
