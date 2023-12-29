package com.emoldino.api.common.resource.base.log.repository.accesssummarylog;

import org.springframework.data.domain.Page;

public interface AccessSummaryLogRepositoryCustom {
	Page<AccessSummaryLog> findMonthly(int pageNumber);
}
