package com.emoldino.api.common.resource.base.log.repository.accesssummarylog;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.emoldino.framework.util.QueryUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;

public class AccessSummaryLogRepositoryImpl extends QuerydslRepositorySupport implements AccessSummaryLogRepositoryCustom {
	public AccessSummaryLogRepositoryImpl() {
		super(AccessSummaryLog.class);
	}

	@Transactional
	@Override
	public Page<AccessSummaryLog> findMonthly(int pageNumber) {
//		SELECT 
//			CALL_MONTH,
//			COMPANY_ID,
//			COMPANY_TYPE,
//			API_NAME,
//			SERVER_NAME,
//			CALLER_INFO,
//			MAX(TARGET_INFO) TARGET_INFO,
//			PARAM_NAMES,
//			SUM(SUCCESS_COUNT) SUCCESS_COUNT,
//			SUM(FAIL_COUNT) FAIL_COUNT,
//			FLOOR(IFNULL(SUM(TOTAL_ELAPSED_TIME_MILLIS) / SUM(SUCCESS_COUNT), 0)) AVG_ELAPSED_TIME_MILLIS,
//			MAX(MAX_ELAPSED_TIME_MILLIS) MAX_ELAPSED_TIME_MILLIS,
//			SUM(TOTAL_ELAPSED_TIME_MILLIS) TOTAL_ELAPSED_TIME_MILLIS
//		FROM 
//			access_summary_log
//		WHERE 
//			CALL_DATE <> 0
//		GROUP BY
//			CALL_MONTH,
//			COMPANY_ID,
//			COMPANY_TYPE,
//			API_NAME,
//			SERVER_NAME,
//			CALLER_INFO,
//			/*TARGET_INFO,*/
//			PARAM_NAMES
//		ORDER BY
//			CALL_MONTH,
//			COMPANY_ID,
//			COMPANY_TYPE,
//			API_NAME,
//			SERVER_NAME,
//			CALLER_INFO,
//			TARGET_INFO,
//			PARAM_NAMES
//		LIMIT 1000;
		QAccessSummaryLog table = QAccessSummaryLog.accessSummaryLog;

		JPQLQuery<AccessSummaryLog> query = from(table);
		query.select(Projections.constructor(AccessSummaryLog.class, table.companyType, table.companyId, table.apiName, table.serverName, table.callerInfo, table.targetInfo.max(),
				table.paramNames, table.callMonth, table.successCount.sum(), table.failCount.sum(),
				table.totalElapsedTimeMillis.sum().divide(table.successCount.sum()).floor().coalesce(0L).asNumber().longValue(), Expressions.constant(0L),
				table.maxElapsedTimeMillis.max(), table.totalElapsedTimeMillis.sum()));
		query.where(table.callDate.ne(0));
		query.groupBy(table.callMonth, table.companyId, table.companyType, table.apiName, table.serverName, table.callerInfo, table.paramNames);
		query.orderBy(table.callMonth.asc(), table.companyId.asc(), table.companyType.asc(), table.apiName.asc(), table.serverName.asc(), table.callerInfo.asc(),
				table.paramNames.asc());
		Pageable pageable = PageRequest.of(pageNumber, 1000);
		getQuerydsl().applyPagination(pageable, query);
		List<AccessSummaryLog> list = query.fetch();
		list.forEach(item -> {
			if (item.getSuccessCount() == 0) {
				return;
			}
//			SELECT 
//				MIN(MIN_ELAPSED_TIME_MILLIS)
//			FROM 
//				access_summary_log
//			WHERE
//				SUCCESS_COUNT > 0
//				AND CALL_DATE <> 0
//				AND COMPANY_ID = asl.COMPANY_ID
//				AND COMPANY_TYPE = asl.COMPANY_TYPE
//				AND API_NAME = asl.API_NAME
//				AND SERVER_NAME = asl.SERVER_NAME
//				AND CALLER_INFO = asl.CALLER_INFO
//				/*AND TARGET_INFO = asl.TARGET_INFO*/
//				AND PARAM_NAMES = asl.PARAM_NAMES
//				AND CALL_MONTH = asl.CALL_MONTH
			BooleanBuilder filter = new BooleanBuilder();
			filter.and(table.successCount.gt(0));
			QueryUtils.and(filter, table.callDate, "<>", 0);
			QueryUtils.and(filter, table.companyId, item.getCompanyId());
			QueryUtils.and(filter, table.companyType, item.getCompanyType());
			QueryUtils.and(filter, table.apiName, item.getApiName());
			QueryUtils.and(filter, table.serverName, item.getServerName());
			QueryUtils.and(filter, table.callerInfo, item.getCallerInfo());
//			QueryUtils.and(filter, table.targetInfo, item.getTargetInfo());
			QueryUtils.and(filter, table.paramNames, item.getParamNames());
			QueryUtils.and(filter, table.callMonth, item.getCallMonth());
			long minElapsedTimeMillis = from(table).select(table.minElapsedTimeMillis.min().coalesce(0L)).where(filter).fetchOne();
			item.setMinElapsedTimeMillis(minElapsedTimeMillis);
		});

		// TODO This Logic is due to the Querydsl Bug, Group By or Distinct Fetch Count Error
		long totalCount = 0;
//		totalCount = query.fetchCount();
		totalCount = pageNumber * 1000L + list.size();
		if (list.size() == 1000) {
			totalCount += 1000;
		}

		Page<AccessSummaryLog> page = new PageImpl<>(list, pageable, totalCount);
		return page;
	}
}
