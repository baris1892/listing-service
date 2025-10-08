package dev.baristop.portfolio.listingservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
@Profile("!test")
public class CacheConfig {

    /**
     * Configures Redis as the caching provider for the application.
     * This CacheManager setup handles:
     * 1. JSON serialization of objects stored in Redis.
     * 2. Proper handling of Java 8 date/time types like Instant and LocalDateTime.
     * 3. Type information inclusion to avoid ClassCastException when reading from cache.
     * <p>
     * Key Points:
     * - GenericJackson2JsonRedisSerializer serializes objects to JSON for flexibility.
     * - JavaTimeModule ensures Instant/LocalDateTime are correctly serialized as ISO-8601 strings.
     * - SerializationFeature.WRITE_DATES_AS_TIMESTAMPS is disabled for readability and interoperability.
     * - activateDefaultTyping adds class metadata to JSON, which is crucial for correct deserialization.
     * <p>
     * Security Note:
     * - activateDefaultTyping is safe here because Redis is internal and trusted.
     * - In public-facing scenarios, prefer Jackson2JsonRedisSerializer<T> for specific types.
     *
     * @param connectionFactory Redis connection factory injected by Spring
     *
     * @return configured CacheManager
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // Enable support for Java 8 date/time types
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // Store dates as ISO-8601 strings
            // Adds type info for deserialization: ensures cached JSON can be converted back to the correct class
            .activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

        RedisSerializer<Object> serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Configure Redis cache: TTL 10 minutes, using our custom serializer for values
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
