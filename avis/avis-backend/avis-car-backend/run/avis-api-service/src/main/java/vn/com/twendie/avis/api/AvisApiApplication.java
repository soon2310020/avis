package vn.com.twendie.avis.api;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import vn.com.twendie.avis.api.converter.*;
import vn.com.twendie.avis.api.mapping.CreateOrUpdateJDDPayloadMappingProperty;
import vn.com.twendie.avis.api.mapping.EditJDDPayloadMappingProperty;
import vn.com.twendie.avis.api.mapping.UpdateUserMappingProperty;
import vn.com.twendie.avis.api.rest.config.EnableRestCommonApi;
import vn.com.twendie.avis.data.config.EnableDataModel;
import vn.com.twendie.avis.locale.config.EnableLocaleModule;
import vn.com.twendie.avis.notification.config.EnableServiceNotification;
import vn.com.twendie.avis.security.config.EnableSecurityModule;
import vn.com.twendie.avis.tracking.config.EnableServiceTrackingGps;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.TimeZone;

@SpringBootApplication
@EnableDataModel
@EnableRestCommonApi
@EnableSecurityModule
@EnableLocaleModule
@EnableJpaRepositories("vn.com.twendie.avis.api.repository")
@EnableRetry(proxyTargetClass = true)
@EnableSwagger2
@EnableCaching
@EnableScheduling
@EnableServiceNotification
@EnableServiceTrackingGps
@Slf4j
public class AvisApiApplication implements CommandLineRunner {

//    @Bean
//    public CacheManager cacheManager() {
//        return new ConcurrentMapCacheManager();
//    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(new JdkSerializationRedisSerializer()));
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public ModelMapper modelMapper(
            ContractCostType2CodeValueModel contractCostType2CodeValueModel,
            ContractNormList2CodeValueModel contractNormList2CodeValueModel,
            JourneyDiaryDailyCostType2CodeValueModel journeyDiaryDailyCostType2CodeValueModel,
            OvertimeReportConverter overtimeReportConverter,
            KmReportConverter kmReportConverter,
            NotificationDTOConverter notificationDTOConverter,
            EditJDDPayloadMappingProperty editJDDPayloadMappingProperty,
            CreateOrUpdateJDDPayloadMappingProperty createOrUpdateJDDPayloadMappingProperty,
            CustomerJourneyDiaryDailyConverter customerJourneyDiaryDailyConverter,
            UpdateUserMappingProperty updateUserMappingProperty) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        modelMapper.addConverter(contractCostType2CodeValueModel);
        modelMapper.addConverter(contractNormList2CodeValueModel);
        modelMapper.addConverter(journeyDiaryDailyCostType2CodeValueModel);
        modelMapper.addConverter(overtimeReportConverter);
        modelMapper.addConverter(kmReportConverter);
        modelMapper.addConverter(notificationDTOConverter);
        modelMapper.addMappings(editJDDPayloadMappingProperty);
        modelMapper.addMappings(createOrUpdateJDDPayloadMappingProperty);
        modelMapper.addConverter(customerJourneyDiaryDailyConverter);
        modelMapper.addMappings(updateUserMappingProperty);
        return modelMapper;
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public void run(String... args) {

    }

    public static void main(String[] args) {
        SpringApplication.run(AvisApiApplication.class, args);
        log.info("Start avis api services");
    }
}
