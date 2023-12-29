package com.stg.config.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Slf4j
@Configuration
public class RedisConfiguration {

    @Value("${spring.redis.host}")
    private String redisHostName;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password}")
    private String redisPassword;

    @Value("${spring.redis.database}")
    private int redisDatabase;

    @Value("${spring.redis.ssl}")
    private boolean redisSsl;

    @Bean
    public ClientResources clientResourcesDefault() {
        return DefaultClientResources.create();
    }

    @Bean
    @Profile({"stg", "default"})
    public RedisCommands<String, String> redis(ClientResources clientResources) {
        RedisURI redisURI = RedisURI.builder()
                .withHost(redisHostName)
                .withPort(redisPort)
                .withPassword(redisPassword)
                .withSsl(redisSsl)
                .withDatabase(redisDatabase)
                .build();

        RedisClient client = RedisClient.create(clientResources, redisURI);

        log.info("Connecting to redis on host={} and port={}", redisHostName, redisPort);

        return client.connect().sync();
    }

    @Bean(name = "redis")
    @Profile({"dev", "test", "dev-docker"})
    public RedisCommands<String, String> redisDev(ClientResources clientResources) {
        RedisURI redisURI = RedisURI.builder()
                .withHost("localhost")
                .withPort(6379)
                .build();

        RedisClient client = RedisClient.create(clientResources, redisURI);

        log.info("Connecting to redis on host={} and port={}", "localhost", 6379);

        return client.connect().sync();
    }

}
