package com.stg.config.redis;

import com.google.common.cache.CacheBuilder;
import com.stg.config.redis.serializer.KryoRedisSerializer;
import com.stg.config.redis.serializer.SnappyRedisSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfiguration {
    public static final String REDIS_CACHING = "redisCacheManager";
    public static final String RAM_CACHING = "memoryCacheManager";

    @Value("${spring.redis.cache.default.expire-time}")
    private  int timeToLiveRedis ;

    @Value("${spring.memory-cache.expire-time}")
    private  int timeToLiveMemory ;

    @Primary
    @Bean(REDIS_CACHING)
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                //.prefixCacheNameWith(this.getClass().getPackageName() + ".")
                .entryTtl(Duration.ofMinutes(timeToLiveRedis))
                .disableCachingNullValues()
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                //.serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(new Jackson().mapper())))
                .serializeValuesWith(SerializationPair.fromSerializer(new SnappyRedisSerializer<>(new KryoRedisSerializer<>(Object.class))));

        return RedisCacheManager.builder(connectionFactory).cacheDefaults(config).build();
    }

    @Bean(RAM_CACHING)
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager() {
            @Override
            protected Cache createConcurrentMapCache(String name) {
                return new ConcurrentMapCache(name, CacheBuilder.newBuilder().expireAfterWrite(timeToLiveMemory, TimeUnit.MINUTES)
                        .maximumSize(1000).build().asMap(), true);
            }
        };
    }
}
