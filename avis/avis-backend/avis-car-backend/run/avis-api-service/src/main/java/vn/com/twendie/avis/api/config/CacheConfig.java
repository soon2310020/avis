package vn.com.twendie.avis.api.config;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import vn.com.twendie.avis.api.service.CacheService;

@Configuration
public class CacheConfig implements ApplicationListener<ContextRefreshedEvent> {

    private final CacheService cacheService;

    public CacheConfig(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent contextRefreshedEvent) {
        cacheService.clearAll();
        cacheService.init();
    }
}
