package com.emoldino.api.common.resource.base.log.service.error;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.api.common.resource.base.log.repository.errorlog.ErrorLog;
import com.emoldino.api.common.resource.base.log.repository.errorlog.ErrorLogRepository;
import com.emoldino.api.common.resource.base.log.repository.errorlog.QErrorLog;
import com.emoldino.api.common.resource.base.log.repository.errorsummarylog.ErrorSummaryLog;
import com.emoldino.api.common.resource.base.log.repository.errorsummarylog.ErrorSummaryLogRepository;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.CacheUtils;
import com.emoldino.framework.util.HttpUtils;
import com.emoldino.framework.util.ServerUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ThreadUtils.ExecutionTime;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import saleson.common.util.SecurityUtils;

@Service
public class ErrorLogService {

	public void save(ErrorLog data) {
		if (data == null) {
			return;
		}

		data.setErrorMessage(ValueUtils.abbreviate(data.getErrorMessage(), 1000));

		// TODO headers, body
//		private String headers;
//		private String body;

		data.setApiName(ThreadUtils.getPropApiName());

		data.setServerName(ServerUtils.getName());

		data.setCallerInfo(ValueUtils.abbreviate(getCallerInfo(), 500));
		data.setTargetInfo(ValueUtils.abbreviate(getTargetInfo(), 500));
		data.setParams(ValueUtils.abbreviate(HttpUtils.getParamsStr(), 2000));

		ExecutionTime time = ThreadUtils.getExecutionTime();
		Instant now = new Date(time.getCurrentTimeMillis()).toInstant();
		if (time.getStartTimeMillis() > 0) {
			data.setStartTime(new Date(time.getStartTimeMillis()).toInstant());
			data.setEndTime(now);
			data.setElapsedTimeMillis(time.getElapsedTimeMillis());
		}
		data.setCreatedBy(SecurityUtils.getUserId());
		data.setCreatedAt(now);
		data.setUpdatedBy(SecurityUtils.getUserId());
		data.setUpdatedAt(now);

		BeanUtils.get(ErrorLogRepository.class).save(data);
	}

	private static final long ONE_WEEK = 60 * 60 * 24 * 7L;

	public void cleanOldBatch() {
		QErrorLog table = QErrorLog.errorLog;
		BooleanBuilder filter = new BooleanBuilder();
		// UPDATED_AT < A Week Ago
		{
			Instant aWeekAgo = Instant.now().minusSeconds(ONE_WEEK);
			filter.and(table.updatedAt.before(aWeekAgo));
		}
		Pageable pageable = PageRequest.of(0, 100, Sort.by(Direction.ASC, "updatedAt"));
		int counter = 0;
		while (_cleanOld(filter, pageable) && counter++ < 10000) {
		}
	}

	private boolean _cleanOld(Predicate predicate, Pageable pageable) {
		return TranUtils.doNewTran(() -> {
			Page<ErrorLog> page = BeanUtils.get(ErrorLogRepository.class).findAll(predicate, pageable);
			if (page.isEmpty()) {
				return false;
			}

			Map<String, ErrorSummaryLog> map = new LinkedHashMap<>();
			page.forEach(item -> {
				// Adjust
				item.setCallerInfo(adjustValue(item.getCallerInfo(), " ", 1));
				item.setTargetInfo(adjustValue(item.getTargetInfo(), "?", 0));

				Instant now = new Date().toInstant();

				// Get or Create Summary Data;
				String key = item.getErrorType().name() + "," + item.getErrorCode() + "," + item.getServerName() + "," + item.getApiName() + "," + item.getCallerInfo() + ","
						+ item.getTargetInfo();
				ErrorSummaryLog data = CacheUtils.get(map, key,
						() -> BeanUtils
								.get(ErrorSummaryLogRepository.class).findByErrorTypeAndErrorCodeAndErrorCauseAndServerNameAndApiNameAndCallerInfoAndTargetInfo(item.getErrorType(),
										item.getErrorCode(), item.getErrorCause(), item.getServerName(), item.getApiName(), item.getCallerInfo(), item.getTargetInfo())
								.orElseGet(() -> {
									ErrorSummaryLog d = new ErrorSummaryLog();
									d.setErrorType(item.getErrorType());
									d.setErrorCode(item.getErrorCode());
									d.setServerName(item.getServerName());
									d.setApiName(item.getApiName());
									d.setCallerInfo(item.getCallerInfo());
									d.setTargetInfo(item.getTargetInfo());
									d.setFirstOccurredTime(item.getCreatedAt());
									d.setCreatedAt(now);
									return d;
								}));

				data.setErrorStatus(item.getErrorStatus());
				data.setErrorMessage(item.getErrorMessage());
				data.setErrorPointPath(item.getErrorPointPath());
				data.setErrorCount(data.getErrorCount() + 1);
				data.setDetailedLog(item.getDetailedLog());
				data.setHeaders(item.getHeaders());
				data.setParams(item.getParams());
				data.setLastOccurredTime(item.getUpdatedAt());
				data.setUpdatedAt(now);
			});

			// Save Summary
			if (!map.isEmpty()) {
				BeanUtils.get(ErrorSummaryLogRepository.class).saveAll(map.values());
			}

			// Delete Old ErrorLog
			BeanUtils.get(ErrorLogRepository.class).deleteAll(page);

			return page.getTotalPages() > page.getNumber() + 1;
		});
	}

	private static String adjustValue(String value, String delimiter, int index) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		if (!value.contains(delimiter)) {
			return value;
		}
		String strs[] = StringUtils.tokenizeToStringArray(value, delimiter);
		String str = strs[Math.min(index, strs.length - 1)];
		return str;
	}

	private static String getCallerInfo() {
		StringBuilder buf = new StringBuilder();

		HttpServletRequest req = HttpUtils.getRequest();
		if (req != null) {
			String host = req.getRemoteHost();
			String addr = req.getRemoteAddr();
			int port = req.getRemotePort();
			if (!ObjectUtils.isEmpty(host)) {
				buf.append(host);
			}
			if (!ObjectUtils.isEmpty(addr) && !addr.equals(host)) {
				buf.append("(").append(addr).append(")");
			}
			buf.append(":").append(port);
		}
		String referer = HttpUtils.getReferer();
		if (!ObjectUtils.isEmpty(referer)) {
			if (buf.length() > 0) {
				buf.append(" ");
			}
			buf.append(referer);
		}

		return buf.length() == 0 ? null : buf.toString();
	}

	private static String getTargetInfo() {
		return HttpUtils.getRequestStr();
	}

}
