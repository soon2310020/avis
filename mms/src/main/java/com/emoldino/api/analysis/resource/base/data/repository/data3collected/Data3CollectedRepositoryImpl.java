package com.emoldino.api.analysis.resource.base.data.repository.data3collected;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class Data3CollectedRepositoryImpl extends QuerydslRepositorySupport {
	public Data3CollectedRepositoryImpl() {
		super(Data3Collected.class);
	}
}
