package com.emoldino.api.common.resource.base.log.repository.errorsummarylog;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;

public interface ErrorSummaryLogRepository extends JpaRepository<ErrorSummaryLog, Long>, QuerydslPredicateExecutor<ErrorSummaryLog> {
	Optional<ErrorSummaryLog> findByErrorTypeAndErrorCodeAndErrorCauseAndServerNameAndApiNameAndCallerInfoAndTargetInfo(ErrorType errorType, String errorCode, String errorCause,
			String serverName, String apiName, String callerInfo, String targetInfo);
}
