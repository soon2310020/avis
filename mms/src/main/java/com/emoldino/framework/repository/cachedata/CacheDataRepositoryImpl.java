package com.emoldino.framework.repository.cachedata;

import org.springframework.data.jpa.repository.support.*;

public class CacheDataRepositoryImpl extends QuerydslRepositorySupport {
	public CacheDataRepositoryImpl() {
		super(CacheData.class);
	}
}
