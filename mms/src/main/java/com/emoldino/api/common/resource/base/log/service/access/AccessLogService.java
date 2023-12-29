package com.emoldino.api.common.resource.base.log.service.access;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.emoldino.api.common.resource.base.log.repository.accesssummarylog.AccessSummaryLog;
import com.emoldino.api.common.resource.base.log.repository.accesssummarylog.AccessSummaryLogRepository;
import com.emoldino.api.common.resource.base.log.repository.accesssummarylog.AccessSummaryLogRepositoryCustom;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.CacheUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.HttpUtils;
import com.emoldino.framework.util.ServerUtils;
import com.emoldino.framework.util.SyncCtrlUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.TranUtils;

import saleson.common.enumeration.CompanyType;
import saleson.common.util.SecurityUtils;

@Service
public class AccessLogService {

	private Optional<AccessSummaryLog> getSummary(CompanyType companyType, Long companyId, String apiName, String serverName, String callerInfo, String targetInfo,
			String paramNames, int callDate, int callHour) {
		Optional<AccessSummaryLog> optional = BeanUtils.get(AccessSummaryLogRepository.class)
				.findByCompanyTypeAndCompanyIdAndApiNameAndServerNameAndCallerInfoAndTargetInfoAndParamNamesAndCallDateAndCallHour(companyType, companyId, apiName, serverName,
						callerInfo, targetInfo, paramNames, callDate, callHour);
		return optional;
	}

	// Key = CompanyType + CompanyId + ApiName + ServerName + CallerUrl + TargetUrl + ParamNames
	// Value = (Key: Date + Hour, Value: SummaryLog)
	private static Map<String, Map<String, AccessSummaryLog>> ACCESS_SUMMARY = new ConcurrentHashMap<>();

	/**
	 * Put Access Summary Log at Memory
	 * 
	 * @param apiName
	 * @param elapsedTime
	 * @param t
	 */
	public void put(String apiName, long elapsedTime, Throwable t) {
		CompanyType companyType = SecurityUtils.getCompanyType();
		Long companyId = SecurityUtils.getCompanyId();
		String _apiName = apiName == null ? ThreadUtils.getPropApiName() : apiName;
		String serverName = ServerUtils.getName();

		String callerInfo = HttpUtils.getReferer();
		String targetInfo = HttpUtils.getRequestUrl();
		String paramNames = null;
		{
			HttpServletRequest req = HttpUtils.getRequest();
			if (req != null) {
				Enumeration<String> items = req.getParameterNames();
				if (items != null && items.hasMoreElements()) {
					StringBuilder buf = new StringBuilder();
					int i = 0;
					do {
						buf.append(i++ == 0 ? "" : ",").append(items.nextElement());
					} while (items.hasMoreElements());
					paramNames = buf.toString();
				}
			}
		}

		String key = new StringBuilder().append(companyType).append(".").append(companyId).append(".").append(_apiName).append(".").append(serverName).append(".")
				.append(callerInfo).append(".").append(targetInfo).append(".").append(paramNames).toString();

		Instant instant = DateUtils2.newInstant();
		String yyyyMMdd = DateUtils2.format(instant, DatePattern.yyyyMMdd, Zone.SYS);
		int month = Integer.parseInt(yyyyMMdd.substring(0, 6));
		int date = Integer.parseInt(yyyyMMdd);
		int hour = Integer.parseInt(DateUtils2.format(instant, DatePattern.HH, Zone.SYS));

		// Key = yyyyMMddHH
		// Value = SummaryLog
		Map<String, AccessSummaryLog> map = SyncCtrlUtils.wrap("ASL." + key, ACCESS_SUMMARY, key, () -> new LinkedHashMap<>());

		final String _callerInfo = callerInfo;
		final String _targetInfo = targetInfo;
		final String _paramNames = paramNames;
		SyncCtrlUtils.wrap("ASL." + key, () -> {
			AccessSummaryLog summary = CacheUtils.get(map, date + "" + hour, () -> {
				AccessSummaryLog data = new AccessSummaryLog();
				data.setCompanyType(companyType);
				data.setCompanyId(companyId);
				data.setApiName(_apiName);
				data.setServerName(serverName);
				data.setCallerInfo(_callerInfo);
				data.setTargetInfo(_targetInfo);
				data.setParamNames(_paramNames);
				data.setCallMonth(month);
				data.setCallDate(date);
				data.setCallHour(hour);
				return data;
			});

			if (t == null) {
				summary.setSuccessCount(summary.getSuccessCount() + 1);
				summary.setTotalElapsedTimeMillis(summary.getTotalElapsedTimeMillis() + elapsedTime);
				if (summary.getSuccessCount() == 1) {
					summary.setMinElapsedTimeMillis(elapsedTime);
				} else {
					summary.setMinElapsedTimeMillis(Math.min(elapsedTime, summary.getMinElapsedTimeMillis()));
				}
				summary.setMaxElapsedTimeMillis(Math.max(elapsedTime, summary.getMaxElapsedTimeMillis()));
			} else {
				summary.setFailCount(summary.getFailCount() + 1);
			}

			// Keeps only 3 Hours data for Saving Memory
			while (map.size() > 3) {
				map.remove(map.keySet().iterator().next());
			}
		});
	}

	/**
	 * Save Access Summary Log Batch from Memory
	 */
	public void saveBatch() {
		if (ACCESS_SUMMARY.isEmpty()) {
			return;
		}

		// Collect Save Target
		List<AccessSummaryLog> list = new ArrayList<>();
		ACCESS_SUMMARY.forEach((key, map) -> {
			SyncCtrlUtils.wrap("ASL." + key, () -> {
				List<String> rlist = new ArrayList<>();
				map.forEach((k, data) -> {
					if (data.getSuccessCount() == 0 && data.getFailCount() == 0) {
						rlist.add(k);
						return;
					}

					AccessSummaryLog item = new AccessSummaryLog();
					item.setCompanyType(data.getCompanyType());
					item.setCompanyId(data.getCompanyId());
					item.setApiName(data.getApiName());
					item.setServerName(data.getServerName());
					item.setCallerInfo(data.getCallerInfo());
					item.setTargetInfo(data.getTargetInfo());
					item.setParamNames(data.getParamNames());
					item.setCallMonth(data.getCallMonth());
					item.setCallDate(data.getCallDate());
					item.setCallHour(data.getCallHour());
					item.setSuccessCount(data.getSuccessCount());
					item.setTotalElapsedTimeMillis(data.getTotalElapsedTimeMillis());
					item.setMinElapsedTimeMillis(data.getMinElapsedTimeMillis());
					item.setMaxElapsedTimeMillis(data.getMaxElapsedTimeMillis());
					item.setFailCount(data.getFailCount());
					list.add(item);

					// Clear Memory
					data.setSuccessCount(0);
					data.setFailCount(0);
					data.setTotalElapsedTimeMillis(0L);
					data.setMinElapsedTimeMillis(0L);
					data.setMaxElapsedTimeMillis(0L);
				});

				rlist.forEach(k -> map.remove(k));

				while (map.size() > 1) {
					map.remove(map.keySet().iterator().next());
				}
			});
		});

		list.forEach(item -> TranUtils.doNewTran(() -> saveSummary(item, true)));

	}

	/**
	 * Reorganize Old Access Summary Log Batch from Repository
	 */
	public void cleanOldBatch() {
		// TODO Open after improving AccessLog Service
//		// Save Monthly Summary Log
//		{
//			int pageNumber = 0;
//			int counter = 0;
//			while (_saveMontly(pageNumber++) && counter++ < 1000) {
//			}
//		}
//
//		// Delete Old Daily Summary Log
//		{
//			QAccessSummaryLog table = QAccessSummaryLog.accessSummaryLog;
//			BooleanBuilder filter = new BooleanBuilder();
//			// CALL_DATE <> 0
//			filter.and(table.callDate.ne(0));
//			// CALL_MONTH <= The month of 50days ago
//			{
//				Instant instant = Instant.now().minus(Duration.ofDays(50));
//				String yyyyMM = DateUtils2.format(instant, DatePattern.yyyyMM, Zone.SYS);
//				filter.and(table.callMonth.loe(Integer.parseInt(yyyyMM)));
//			}
//
//			TranUtils.doNewTran(() -> BeanUtils.get(JPAQueryFactory.class).delete(table).where(filter).execute());
//
////			Pageable pageable = PageRequest.of(0, 1000, Sort.by(Direction.ASC, "updatedAt"));
////			int counter = 0;
////			while (_cleanOld(filter, pageable) && counter++ < 1000) {
////			}
//		}
	}

	private boolean _saveMontly(int pageNumber) {
		return TranUtils.doNewTran(() -> {
			Page<AccessSummaryLog> page = BeanUtils.get(AccessSummaryLogRepositoryCustom.class).findMonthly(pageNumber);
			page.forEach(item -> saveSummary(item, false));
			return page.getTotalPages() > page.getNumber() + 1;
		});
	}

//	private boolean _cleanOld(Predicate predicate, Pageable pageable) {
//		return TranUtils.doNewTran(() -> {
//			Page<AccessSummaryLog> page = BeanUtils.get(AccessSummaryLogRepository.class).findAll(predicate, pageable);
//			if (page.isEmpty()) {
//				return false;
//			}
//			BeanUtils.get(AccessSummaryLogRepository.class).deleteAll(page.getContent());
//			return page.getTotalPages() > page.getNumber() + 1;
//		});
//	}

	private void saveSummary(AccessSummaryLog item, boolean append) {
		AccessSummaryLog prevItem = getSummary(item.getCompanyType(), item.getCompanyId(), item.getApiName(), item.getServerName(), item.getCallerInfo(), item.getTargetInfo(),
				item.getParamNames(), item.getCallDate(), item.getCallHour()).orElse(null);
		Instant now = new Date().toInstant();
		AccessSummaryLog data;
		if (prevItem == null) {
			data = item;
			data.setCreatedAt(now);
			if (item.getTotalElapsedTimeMillis() != 0 && item.getSuccessCount() != 0) {
				// Avg Time = New Total Time / New Success Count
				data.setAvgElapsedTimeMillis(item.getTotalElapsedTimeMillis() / item.getSuccessCount());
			}
		} else {
			data = prevItem;
			if (append) {
				data.setSuccessCount(prevItem.getSuccessCount() + item.getSuccessCount());
				data.setFailCount(prevItem.getFailCount() + item.getFailCount());
				// TODO For Backward Compatible Temporarily
				if (prevItem.getTotalElapsedTimeMillis() == 0 && prevItem.getAvgElapsedTimeMillis() != 0) {
					prevItem.setTotalElapsedTimeMillis(prevItem.getAvgElapsedTimeMillis() * prevItem.getSuccessCount());
				}
				if (item.getTotalElapsedTimeMillis() != 0 && item.getSuccessCount() != 0) {
					data.setTotalElapsedTimeMillis(prevItem.getTotalElapsedTimeMillis() + item.getTotalElapsedTimeMillis());
					// Avg Time = (Old Total Time + New Total Time) / (Old Success Count + New Success Count)
					data.setAvgElapsedTimeMillis(data.getTotalElapsedTimeMillis() / data.getSuccessCount());
					// Min Time
					if (prevItem.getSuccessCount() == 0) {
						data.setMinElapsedTimeMillis(item.getMinElapsedTimeMillis());
					} else {
						data.setMinElapsedTimeMillis(Math.min(prevItem.getMinElapsedTimeMillis(), item.getMinElapsedTimeMillis()));
					}
					// Max Time
					data.setMaxElapsedTimeMillis(Math.max(prevItem.getMaxElapsedTimeMillis(), item.getMaxElapsedTimeMillis()));
				}
			} else {
				if (data.getSuccessCount() == item.getSuccessCount() && data.getFailCount() == item.getFailCount()
						&& data.getTotalElapsedTimeMillis() == item.getTotalElapsedTimeMillis() && data.getAvgElapsedTimeMillis() == item.getAvgElapsedTimeMillis()
						&& data.getMinElapsedTimeMillis() == item.getMinElapsedTimeMillis() && data.getMaxElapsedTimeMillis() == item.getMaxElapsedTimeMillis()) {
					return;
				}
				data.setSuccessCount(item.getSuccessCount());
				data.setFailCount(item.getFailCount());
				data.setTotalElapsedTimeMillis(item.getTotalElapsedTimeMillis());
				data.setAvgElapsedTimeMillis(item.getAvgElapsedTimeMillis());
				data.setMinElapsedTimeMillis(item.getMinElapsedTimeMillis());
				data.setMaxElapsedTimeMillis(item.getMaxElapsedTimeMillis());
			}
		}
		data.setUpdatedAt(now);
		BeanUtils.get(AccessSummaryLogRepository.class).save(data);
	}

}
