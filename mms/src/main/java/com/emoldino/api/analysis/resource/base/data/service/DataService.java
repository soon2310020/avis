package com.emoldino.api.analysis.resource.base.data.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.SyncCtrlUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import lombok.extern.slf4j.Slf4j;
import saleson.api.mold.MoldRepository;
import saleson.api.statistics.StatisticsRepository;
import saleson.api.statistics.payload.StatisticsDaily;
import saleson.common.enumeration.PeriodType;
import saleson.model.Mold;
import saleson.model.QMold;
import saleson.model.QStatistics;
import saleson.model.Statistics;
import saleson.model.data.QStatisticsSummary;
import saleson.model.data.StatisticsSummary;
import saleson.model.data.StatisticsSummaryRepository;

@Slf4j
@Service("dataService2")
public class DataService {

	@Transactional(propagation = Propagation.NEVER)
	public void adjustBatch() {
		JobUtils.runIfNotRunning("analysis.DataService.adjustBatch", new JobOptions().setClustered(true), () -> {
			String date = DateUtils2.getString(DatePattern.yyyyMMdd, Zone.GMT);
			if (date.endsWith("0101") || date.endsWith("1231")) {
				return;
			}

//			String fromYear = "2036";
			String yearsLater = get2YearsLater(Zone.GMT);
			String thisYear = getThisYear(Zone.GMT);

//			{
//				Page<Mold> page;
//				int pageNo[] = { 0 };
//				int pageSize = 100;
//				while (pageNo[0] < 10000 && !(page = TranUtils.doNewTran(() -> BeanUtils.get(MoldRepository.class).findAll(PageRequest.of(pageNo[0]++, pageSize)))).isEmpty()) {
//					page.forEach(mold -> TranUtils.doNewTran(() -> {
//						mold.setLastShotAt(replacePrefix(mold.getLastShotAt()));
//						mold.setLastShotMadeAt(replacePrefix(mold.getLastShotMadeAt()));
//						BeanUtils.get(MoldRepository.class).save(mold);
//					}));
//				}
//			}

			// Statistics
			{
				int i = 0;
				QStatistics table = QStatistics.statistics;
				BooleanBuilder filter = new BooleanBuilder().and(table.year.goe(yearsLater));
				PageRequest pageable = PageRequest.of(0, 1000);

				if (TranUtils.doNewTran(() -> BeanUtils.get(StatisticsRepository.class).exists(filter))) {
					do {
					} while (i++ < 1000 && SyncCtrlUtils.wrap("analysis.DataService.adjustBatch", () -> {
						Page<Statistics> page = TranUtils.doNewTran(() -> BeanUtils.get(StatisticsRepository.class).findAll(filter, pageable));
						if (page.isEmpty()) {
							return false;
						}
						page.forEach(item -> {
							String fromYear = item.getYear();
							item.setYear(thisYear);
							item.setMonth(replacePrefix(item.getMonth(), fromYear, thisYear));
							item.setDay(replacePrefix(item.getDay(), fromYear, thisYear));
							item.setWeek(replacePrefix(item.getWeek(), fromYear, thisYear));
							item.setHour(replacePrefix(item.getHour(), fromYear, thisYear));
							TranUtils.doNewTran(() -> BeanUtils.get(StatisticsRepository.class).save(item));
						});
						return true;
					}));
				}
			}

			Instant tomorrow = DateUtils2.getInstant().plus(Duration.ofDays(1));

			// Molds
			{
				int i = 0;
				QMold table = QMold.mold;
				BooleanBuilder filter = new BooleanBuilder().and(table.lastShotAt.goe(tomorrow));
				PageRequest pageable = PageRequest.of(0, 1000);

				if (TranUtils.doNewTran(() -> BeanUtils.get(MoldRepository.class).exists(filter))) {
					do {
					} while (i++ < 1000 && SyncCtrlUtils.wrap("analysis.DataService.adjustBatch", () -> {
						Page<Mold> page = TranUtils.doNewTran(() -> BeanUtils.get(MoldRepository.class).findAll(filter, pageable));
						if (page.isEmpty()) {
							return false;
						}
						page.forEach(item -> TranUtils.doNewTran(() -> {
							Mold mold = BeanUtils.get(MoldRepository.class).findWithPessimisticLockById(item.getId()).orElse(null);
							mold.setLastShotAt(DateUtils2.adjust(item.getLastShotAt(), DateUtils2.getInstant()));
							BeanUtils.get(MoldRepository.class).save(mold);
						}));
						return true;
					}));
				}
			}
			{
				int i = 0;
				QMold table = QMold.mold;
				BooleanBuilder filter = new BooleanBuilder().and(table.lastShotMadeAt.goe(tomorrow));
				PageRequest pageable = PageRequest.of(0, 1000);

				if (TranUtils.doNewTran(() -> BeanUtils.get(MoldRepository.class).exists(filter))) {
					do {
					} while (i++ < 1000 && SyncCtrlUtils.wrap("analysis.DataService.adjustBatch", () -> {
						Page<Mold> page = TranUtils.doNewTran(() -> BeanUtils.get(MoldRepository.class).findAll(filter, pageable));
						if (page.isEmpty()) {
							return false;
						}
						page.forEach(item -> TranUtils.doNewTran(() -> {
							Mold mold = BeanUtils.get(MoldRepository.class).findWithPessimisticLockById(item.getId()).orElse(null);
							mold.setLastShotMadeAt(DateUtils2.adjust(item.getLastShotMadeAt(), DateUtils2.getInstant()));
							BeanUtils.get(MoldRepository.class).save(mold);
						}));
						return true;
					}));
				}
			}
			{
				int i = 0;
				QMold table = QMold.mold;
				BooleanBuilder filter = new BooleanBuilder().and(table.lastShotAtVal.goe(tomorrow));
				PageRequest pageable = PageRequest.of(0, 1000);

				if (TranUtils.doNewTran(() -> BeanUtils.get(MoldRepository.class).exists(filter))) {
					do {
					} while (i++ < 1000 && SyncCtrlUtils.wrap("analysis.DataService.adjustBatch", () -> {
						Page<Mold> page = TranUtils.doNewTran(() -> BeanUtils.get(MoldRepository.class).findAll(filter, pageable));
						if (page.isEmpty()) {
							return false;
						}
						page.forEach(item -> TranUtils.doNewTran(() -> {
							Mold mold = BeanUtils.get(MoldRepository.class).findWithPessimisticLockById(item.getId()).orElse(null);
							mold.setLastShotAtVal(DateUtils2.adjust(item.getLastShotAtVal(), DateUtils2.getInstant()));
							BeanUtils.get(MoldRepository.class).save(mold);
						}));
						return true;
					}));
				}
			}
		});
	}

//	private static Instant replacePrefix(Instant instant) {
//		if (instant == null) {
//			return null;
//		}
//		String str = DateUtils2.format(instant, DatePattern.yyyyMMddHHmmss, Zone.SYS);
//		if (str.startsWith("2036")) {
//			instant = DateUtils2.toInstant(replacePrefix(str, "2036", getThisYear(Zone.SYS)), DatePattern.yyyyMMddHHmmss, Zone.SYS);
//		}
//		return instant;
//	}

	private static String replacePrefix(String str, String fromStr, String toStr) {
		if (ObjectUtils.isEmpty(str) || fromStr == null || toStr == null || !str.startsWith(fromStr)) {
			return str;
		}

		String _str = str.substring(fromStr.length());
		return toStr + _str;
	}

	private static final String PROP_THISYEAR = "DataService.thisYear";

	private static String getThisYear(String zoneId) {
		return ThreadUtils.getProp(PROP_THISYEAR, () -> DateUtils2.format(DateUtils2.getInstant(), DatePattern.yyyy, zoneId));
	}

	private static final String PROP_2YEARSLATER = "DataService.2yearsLater";

	private static String get2YearsLater(String zoneId) {
		return ThreadUtils.getProp(PROP_2YEARSLATER, () -> ValueUtils.toInteger(DateUtils2.format(DateUtils2.getInstant(), DatePattern.yyyy, zoneId), null) + 2 + "");
	}

	public void cleanBatch() {
		_cleanBatch(false);
	}

	public void rebuildBatch() {
		_cleanBatch(true);
	}

	private void _cleanBatch(boolean rebuild) {
		JobUtils.runIfNotRunning("analysis.DataService.cleanBatch", new JobOptions().setClustered(true), () -> {
			int durationDays = 50;
			Instant instant = DateUtils2.newInstant().minus(Duration.ofDays(durationDays));
			String fiftyDaysAgo = DateUtils2.format(instant, DatePattern.yyyyMMdd, Zone.SYS);

//			// 1. Adjust
//			{
//				int pageSize = 1000;
//
//				// TODO Open
//				// 1.1 Cdata.moldId, moldCode -> Statistics.moldId, moldCode Copy
//				// UPDATE statistics stt, cdata cdt SET stt.MOLD_ID = cdt.MOLD_ID, stt.MOLD_CODE = cdt.MOLD_CODE
//				// WHERE cdt.ID = stt.CDATA_ID AND cdt.MOLD_ID IS NOT NULL AND stt.MOLD_ID IS NULL AND stt.DAY >= :day
////				int count = TranUtils
////						.doNewTran(() -> BeanUtils.get(StatisticsRepository.class).updateAllMoldIdAndMoldCodeByCdataAndMoldIdIsNullAndDayGreaterThanEqual(fiftyDaysAgo));
////				if (count > 0) {
////					log.info("Updated Count of Statistics MoldId and MoldCode by Cdata is " + count);
////				}
//
//				List<String> warns = new ArrayList<String>();
//
//				// 1.2 First Statistics.moldId input -> Statistics.moldId, moldCode, Cdata.moldId, moldCode Copy
//				int[] pageNumber = { 0 };
//				do {
//				} while (TranUtils.doNewTran(() -> {
//					// SELECT DISTINCT CI FROM Statistics WHERE MOLD_ID IS NULL AND CDATA_ID IS NOT NULL AND DAY >= :day LIMIT 100;
//					List<String> list = BeanUtils.get(StatisticsRepository.class).findDistinctCiByMoldIdIsNullAndCdataIdIsNotNullAndDayGreaterThanEqual(fiftyDaysAgo,
//							PageRequest.of(pageNumber[0]++, pageSize));
//					if (list.isEmpty()) {
//						return false;
//					}
//
//					Map<String, Statistics> adjusts = new LinkedHashMap<>();
//
//					// SELECT * FROM statistics WHERE CI = :ci AND MOLD_ID IS NOT NULL AND DAY >= :day ORDER BY HOUR ASC LIMIT 1;
//					list.forEach(ci -> {
//						Optional<Statistics> optional = BeanUtils.get(StatisticsRepository.class).findFirstByCiAndMoldIdIsNotNullAndDayGreaterThanEqualOrderByHourAsc(ci,
//								fiftyDaysAgo);
//						if (!optional.isPresent()) {
//							warns.add(ci);
//							return;
//						}
//
//						// TODO Open
////						Statistics item = optional.get();
////						Statistics stat = new Statistics();
////						stat.setMoldId(item.getMoldId());
////						stat.setMoldCode(item.getMoldCode());
////						adjusts.put(ci, stat);
//					});
//
//					adjusts.forEach((ci, stat) -> TranUtils.doNewTran(() -> {
//						// UPDATE Statistics SET moldId = :moldId, moldCode = :moldCode WHERE ci = :ci AND moldId IS NULL AND cdataId IS NOT NULL AND day >= :from AND day <= :to
//						int ssize = BeanUtils.get(StatisticsRepository.class).updateAllMoldIdAndMoldCodeByCiAndMoldIdIsNullAndCdataIdIsNotNullAndDayBetween(ci, stat.getMoldId(),
//								stat.getMoldCode(), fiftyDaysAgo, stat.getDay());
//						// UPDATE Cdata SET moldId = :moldId, moldCode = :moldCode WHERE ci = :ci AND moldId IS NULL AND day >= :from AND day <= :to
//						int csize = BeanUtils.get(CdataRepository.class).updateAllMoldIdAndMoldCodeByCiAndMoldIdIsNullAndDayBetween(ci, stat.getMoldId(), stat.getMoldCode(),
//								fiftyDaysAgo, stat.getDay());
//						log.info("Updated Count of Statistics MoldId and MoldCode is " + ssize + " and Cdata MoldId and MoldCode is " + csize);
//					}));
//
//					return true;
//				}));
//
//				// 1.3 COUNTER_AND_MOLD_NOT_MATCHED Warning
//				if (!warns.isEmpty()) {
//					String message;
//					{
//						int size = warns.size();
//						StringBuilder buf = new StringBuilder();
//						if (size == 1) {
//							buf.append("There is a counter that is not matched to a mold, yet!! Counter Code: " + warns.get(0));
//						} else {
//							buf.append("There are ").append(size).append(" counters those are not matched to molds!! Counter Code: ").append(warns.get(0)).append(" (and other ")
//									.append(size - 1).append(" Counters)");
//						}
//						message = buf.toString();
//					}
//					LogUtils.saveErrorQuietly(ErrorType.WARN, "COUNTER_AND_MOLD_NOT_MATCHED", HttpStatus.NOT_FOUND, message, StringUtils.collectionToCommaDelimitedString(warns));
//				}
//			}
//
//			adjustBatch();

			// 2. Statistics Summary

			// Daily
			Integer after;
			if (rebuild) {
				after = null;
			} else {
				after = TranUtils.doNewTran(() -> {
					QStatisticsSummary table = QStatisticsSummary.statisticsSummary;
					BooleanBuilder filter = new BooleanBuilder();
					filter.and(table.summaryStatus.eq("COMPLETED"));
					filter.and(table.summaryGroup.eq("MOLD_AND_CI"));
					filter.and(table.periodType.eq(PeriodType.DAILY));
					Sort sort = Sort.by(Direction.DESC, "day");
					Page<StatisticsSummary> page = BeanUtils.get(StatisticsSummaryRepository.class).findAll(filter, PageRequest.of(0, 1, sort));
					if (page.isEmpty()) {
						BeanUtils.get(StatisticsSummaryRepository.class).deleteAllByDayGreaterThan(0);
						return null;
					}

					int day = Math.min(ValueUtils.toInteger(fiftyDaysAgo, 0), page.getContent().get(0).getDay());
//					if (day > 0) {
//						BeanUtils.get(StatisticsSummaryRepository.class).deleteAllByDayGreaterThan(day);
//					}
					return day;
				});
			}

			{
				Map<Integer, List<Long>> idMap = new TreeMap<>();
				int pageSize = 1000;
				int i[] = { 0 };

//				Long[] lastId = { null };
				String[] lastDay = { null };
				Long[] lastMoldId = { null };
				String[] lastCi = { null };
				boolean next = true;
				while (next && i[0]++ < 500) {
					next = TranUtils.doNewTran(() -> {
//						Page<Statistics> page;
//						{
//							QStatistics table = QStatistics.statistics;
//							BooleanBuilder filter = new BooleanBuilder();
//							if (lastId[0] != null) {
//								filter.and(table.id.gt(lastId[0]));
//							}
//							filter.and(table.moldId.isNotNull());
//							filter.and(table.ci.isNotNull());
//							filter.and(table.shotCount.gt(0));
//							if (after != null) {
//								filter.and(table.day.gt(after + ""));
//							}
//							filter.and(table.day.lt(before));
//							Sort sort = Sort.by(Direction.ASC, "id");
//							page = BeanUtils.get(StatisticsRepository.class).findAll(filter, PageRequest.of(0, pageSize, sort));
//
//							if (page.isEmpty()) {
//								return false;
//							}
//						}

						List<StatisticsDaily> list = BeanUtils.get(StatisticsRepository.class).findAllGroupByYearAndMonthAndDayAndWeekAndMoldIdAndCi(ValueUtils.toString(after),
								null, lastDay[0], lastMoldId[0], lastCi[0], PageRequest.of(0, pageSize));

						if (list.isEmpty()) {
							return false;
						}

						list.forEach(item -> {
							int year = ValueUtils.toInteger(item.getYear(), 0);
							int month = ValueUtils.toInteger(item.getMonth(), 0);
							int day = ValueUtils.toInteger(item.getDay(), 0);
							int week = ValueUtils.toInteger(item.getWeek(), 0);

							QStatisticsSummary table = QStatisticsSummary.statisticsSummary;
							BooleanBuilder filter = new BooleanBuilder();
							filter.and(table.summaryGroup.eq("MOLD_AND_CI"));
							filter.and(table.periodType.eq(PeriodType.DAILY));
							filter.and(table.year.eq(year));
							filter.and(table.month.eq(month));
							filter.and(table.day.eq(day));
							filter.and(table.week.eq(week));
							filter.and(table.moldId.eq(item.getMoldId()));
							filter.and(table.ci.eq(item.getCi()));
							StatisticsSummary data = BeanUtils.get(StatisticsSummaryRepository.class).findOne(filter)
									.orElse(new StatisticsSummary("MOLD_AND_CI", PeriodType.DAILY, year, month, day, week, item.getMoldId(), item.getCi()));

							data.setUptimeSeconds(item.getUptimeSeconds());
							data.setCt(item.getCt());
							data.setCtVal(item.getCtVal());
							data.setShotCount(item.getShotCount());
							data.setShotCountVal(item.getShotCountVal());

							BeanUtils.get(StatisticsSummaryRepository.class).save(data);

							if (data.getId() == null) {
								data = BeanUtils.get(StatisticsSummaryRepository.class).findOne(filter).get();
							}

							if (!"COMPLETED".equals(data.getSummaryStatus())) {
								List<Long> idList;
								if (idMap.containsKey(day)) {
									idList = idMap.get(day);
								} else {
									idList = new ArrayList<>();
									idMap.put(day, idList);
								}
								idList.add(data.getId());
							}
//							lastId[0] = item.getId();
							lastDay[0] = item.getDay();
							lastMoldId[0] = item.getMoldId();
							lastCi[0] = item.getCi();
						});

						return true;
					});

					while (idMap.size() >= 3) {
						List<Long> ids = idMap.remove(idMap.keySet().iterator().next());
						ids.forEach(id -> TranUtils.doNewTran(() -> {
							StatisticsSummary data = BeanUtils.get(StatisticsSummaryRepository.class).findById(id).get();
							data.setSummaryStatus("COMPLETED");
							BeanUtils.get(StatisticsSummaryRepository.class).save(data);
						}));
					}
				}

				if (i[0] >= 500) {
					LogUtils.saveErrorQuietly(ErrorType.WARN, "DOUBT_INFINITE_LOOP", HttpStatus.EXPECTATION_FAILED, "Loop Count was bigger than 500");
				}

				idMap.values().forEach(ids -> ids.forEach(id -> TranUtils.doNewTran(() -> {
					StatisticsSummary data = BeanUtils.get(StatisticsSummaryRepository.class).findById(id).get();
					data.setSummaryStatus("COMPLETED");
					BeanUtils.get(StatisticsSummaryRepository.class).save(data);
				})));

				if (lastDay[0] != null) {
					TranUtils.doNewTran(() -> BeanUtils.get(StatisticsSummaryRepository.class).deleteAllByDayGreaterThan(ValueUtils.toInteger(lastDay[0], 0)));
				}
			}

			// TODO Weekly
			{

			}

			// TODO Monthly
			{

			}

			// TODO Yearly
			{

			}
		});
	}
}
