package com.stg.repository.sale;

import static com.stg.config.redis.CacheConfiguration.RAM_CACHING;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.stg.entity.sale.BranchInformation;

public interface BranchInformationRepository extends JpaRepository<BranchInformation, Long> {

    @Cacheable(value = "cache:branchs", cacheManager = RAM_CACHING)
    Optional<BranchInformation> findByCode(String code);
    
}
