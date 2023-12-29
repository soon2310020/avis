package com.emoldino.api.common.resource.base.log.repository.errorsummarylog;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class ErrorSummaryLogRepositoryImpl extends QuerydslRepositorySupport {
	public ErrorSummaryLogRepositoryImpl() {
		super(ErrorSummaryLog.class);
	}
}
