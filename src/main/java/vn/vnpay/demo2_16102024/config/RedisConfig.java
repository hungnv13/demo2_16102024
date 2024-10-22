package vn.vnpay.demo2_16102024.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        logger.info("Creating JedisPoolConfig with maxTotal=10, maxIdle=5, minIdle=2");
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(2);
        logger.info("JedisPoolConfig created successfully");

        logger.info("Creating JedisConnectionFactory with the configured pool");
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(poolConfig);
        logger.info("JedisConnectionFactory created successfully");

        return jedisConnectionFactory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        logger.info("Creating RedisTemplate with provided RedisConnectionFactory");
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        logger.info("RedisTemplate created successfully");

        return template;
    }
}
