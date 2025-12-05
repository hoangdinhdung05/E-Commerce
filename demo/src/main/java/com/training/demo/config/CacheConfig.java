package com.training.demo.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration
 * Configures caching strategy for different entity types
 */
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    /**
     * Cache names constants
     */
    public static final String PRODUCT_CACHE = "products";
    public static final String PRODUCT_LIST_CACHE = "productList";
    public static final String CATEGORY_CACHE = "categories";
    public static final String CATEGORY_LIST_CACHE = "categoryList";
    public static final String USER_CACHE = "users";

    /**
     * Cache TTL constants (in minutes)
     */
    private static final int PRODUCT_CACHE_TTL = 60;      // 1 hour
    private static final int CATEGORY_CACHE_TTL = 120;    // 2 hours  
    private static final int USER_CACHE_TTL = 30;         // 30 minutes
    private static final int LIST_CACHE_TTL = 15;         // 15 minutes

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        log.info("Configuring Redis Cache Manager");

        // Configure Object Mapper for Redis serialization
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer(objectMapper)
                        )
                )
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(10)); // Default 10 minutes

        // Specific cache configurations with different TTLs
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        cacheConfigurations.put(PRODUCT_CACHE, 
                defaultConfig.entryTtl(Duration.ofMinutes(PRODUCT_CACHE_TTL)));
        
        cacheConfigurations.put(PRODUCT_LIST_CACHE, 
                defaultConfig.entryTtl(Duration.ofMinutes(LIST_CACHE_TTL)));
        
        cacheConfigurations.put(CATEGORY_CACHE, 
                defaultConfig.entryTtl(Duration.ofMinutes(CATEGORY_CACHE_TTL)));
        
        cacheConfigurations.put(CATEGORY_LIST_CACHE, 
                defaultConfig.entryTtl(Duration.ofMinutes(LIST_CACHE_TTL)));
        
        cacheConfigurations.put(USER_CACHE, 
                defaultConfig.entryTtl(Duration.ofMinutes(USER_CACHE_TTL)));

        log.info("Cache configurations: {} caches registered", cacheConfigurations.size());

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
