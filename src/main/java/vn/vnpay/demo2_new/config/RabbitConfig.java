package vn.vnpay.demo2_new.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String QUEUE_NAME = "paymentQueue"; // Tên hàng đợi
    public static final String EXCHANGE_NAME = "paymentExchange"; // Tên exchange
    public static final String ROUTING_KEY = "paymentRoutingKey"; // Tên routing key

    @Bean
    public Queue paymentQueue() {
        return new Queue(QUEUE_NAME, true); // Tạo hàng đợi
    }

    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(EXCHANGE_NAME); // Tạo exchange với kiểu direct
    }
}
