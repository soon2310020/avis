package com.emoldino.framework.repository.cachedata;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.querydsl.*;

public interface CacheDataRepository extends JpaRepository<CacheData, Long>, QuerydslPredicateExecutor<CacheData> {
	int deleteByCacheNameAndCacheKey(String cacheName, String cacheKey);
}
