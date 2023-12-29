package com.emoldino.api.common.resource.base.log.repository.accesssummarylog;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import saleson.common.enumeration.CompanyType;

public interface AccessSummaryLogRepository extends JpaRepository<AccessSummaryLog, Long>, QuerydslPredicateExecutor<AccessSummaryLog> {

	Optional<AccessSummaryLog> findByCompanyTypeAndCompanyIdAndApiNameAndServerNameAndCallerInfoAndTargetInfoAndParamNamesAndCallDateAndCallHour(CompanyType companyType,
			Long companyId, String apiName, String serverName, String callerInfo, String targetInfo, String paramNames, int callDate, int callHour);

}
