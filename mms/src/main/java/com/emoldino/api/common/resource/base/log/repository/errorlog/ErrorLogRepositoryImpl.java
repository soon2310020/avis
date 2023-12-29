package com.emoldino.api.common.resource.base.log.repository.errorlog;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class ErrorLogRepositoryImpl extends QuerydslRepositorySupport {
	public ErrorLogRepositoryImpl() {
		super(ErrorLog.class);
	}
}
