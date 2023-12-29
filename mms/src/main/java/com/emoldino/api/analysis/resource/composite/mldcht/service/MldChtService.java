package com.emoldino.api.analysis.resource.composite.mldcht.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter.CycleTime;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounterRepository;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.QDataCounter;
import com.emoldino.api.analysis.resource.base.data.repository.moldchartstat.MoldChartStatRepository;
import com.emoldino.api.analysis.resource.base.data.repository.moldchartstat.QMoldChartStat;
import com.emoldino.api.analysis.resource.composite.mldcht.dto.get.MldChtGetIn;
import com.emoldino.api.analysis.resource.composite.mldcht.dto.get.MldChtGetOut;
import com.emoldino.api.analysis.resource.composite.mldcht.dto.get.MldChtGetOut.MldChtItem;
import com.emoldino.api.analysis.resource.composite.mldcht.dto.get.MldChtGetOut.MldChtItemPart;
import com.emoldino.api.analysis.resource.composite.mldcht.dto.getoptions.MldChtGetOptionsIn;
import com.emoldino.api.analysis.resource.composite.mldcht.dto.getoptions.MldChtGetOptionsOut;
import com.emoldino.api.analysis.resource.composite.mldcht.enumeration.MldChtDataGroup;
import com.emoldino.api.analysis.resource.composite.mldcht.enumeration.MldChtTimeScale;
import com.emoldino.api.asset.resource.base.mold.dto.OptimalCycleTime;
import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.Closure1Param;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.Property;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.api.chart.ChartController;
import saleson.api.chart.payload.ChartPayload;
import saleson.api.mold.DynamicExportService;
import saleson.api.mold.MoldRepository;
import saleson.api.mold.MoldRepositoryImpl;
import saleson.common.enumeration.ChartDataType;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.DateViewType;
import saleson.model.Counter;
import saleson.model.Mold;
import saleson.model.data.AvgCavityStatisticsData;
import saleson.model.data.ChartData;
import saleson.model.data.PartShotData;

@Service
public class MldChtService {
	private static final int MAX_COUNT = 480;

	private static final int MINUTE = 60;
	private static final int MAX_MINUTE = MINUTE * MAX_COUNT;

	private static final int HOUR = MINUTE * 60;
	private static final int MAX_HOURS = HOUR * MAX_COUNT;

	private static final int DAY = HOUR * 24;
	private static final int MAX_DAYS = DAY * MAX_COUNT;

	private static final int WEEK = DAY * 7;

	private static final int MAX_WEEKS = WEEK * MAX_COUNT;
	private static final int MAX_MONTHS = DAY * 30 * MAX_COUNT;

	public MldChtGetOut get(MldChtGetIn input) {
		/**
		 * 1. Validation
		 */

		ValueUtils.assertNotEmpty(input.getMoldId(), "moldId");
		ValueUtils.assertNotEmpty(input.getDataGroup(), "dataGroup");
		ValueUtils.assertNotEmpty(input.getTimeScale(), "timeScale");

		Mold mold = BeanUtils.get(MoldRepository.class)//
				.findById(input.getMoldId())//
				.orElseThrow(() -> DataUtils.newDataNotFoundException(Mold.class, "id", input.getMoldId()));

		String zoneId = LocationUtils.getZoneIdByLocationId(mold.getLocationId());

		Instant fromDate = adjustDate(input.getFromDate(), "fromDate", "FROM", zoneId);
		Instant toDate = adjustDate(input.getToDate(), "toDate", "TO", zoneId);
		// Period Validation
		{
			long period = toDate.getEpochSecond() - fromDate.getEpochSecond();
			if (period < 0) {
				return new MldChtGetOut();
			}

			if (MldChtTimeScale.MINUTELY.equals(input.getTimeScale())) {
				if (period > MAX_MINUTE) {
					LogicException e = LogicUtils.newOverPeriodException("Mold Chart Minutely", MAX_COUNT + " Minutes", input.getFromDate(), input.getToDate());
					throw e;
				}
			} else if (MldChtTimeScale.HOURLY.equals(input.getTimeScale())) {
				if (period > MAX_HOURS) {
					LogicException e = LogicUtils.newOverPeriodException("Mold Chart Hourly", MAX_COUNT + " Hours", input.getFromDate(), input.getToDate());
					throw e;
				}
			} else if (MldChtTimeScale.DAILY.equals(input.getTimeScale())) {
				if (period > MAX_DAYS) {
					LogicException e = LogicUtils.newOverPeriodException("Mold Chart Daily", MAX_COUNT + " Days", input.getFromDate(), input.getToDate());
					throw e;
				}
			} else if (MldChtTimeScale.WEEKLY.equals(input.getTimeScale())) {
				if (period > MAX_WEEKS) {
					LogicException e = LogicUtils.newOverPeriodException("Mold Chart Weekly", MAX_COUNT + " Weeks", input.getFromDate(), input.getToDate());
					throw e;
				}
			} else if (MldChtTimeScale.MONTHLY.equals(input.getTimeScale())) {
				if (period > MAX_MONTHS) {
					LogicException e = LogicUtils.newOverPeriodException("Mold Chart Monthly", MAX_COUNT + " Months", input.getFromDate(), input.getToDate());
					throw e;
				}
			}
		}

		/**
		 * 2. Logic
		 */

		MldChtGetOut output = new MldChtGetOut();

		Map<String, MldChtItem> items = new LinkedHashMap<>();
		if (MldChtTimeScale.MINUTELY.equals(input.getTimeScale())) {
			putItems(items, fromDate, toDate, MINUTE, DatePattern.yyyyMMddHHmm, zoneId);
		} else if (MldChtTimeScale.HOURLY.equals(input.getTimeScale())) {
			putItems(items, fromDate, toDate, HOUR, DatePattern.yyyyMMddHH, zoneId);
		} else if (MldChtTimeScale.DAILY.equals(input.getTimeScale())) {
			putItems(items, fromDate, toDate, DAY, DatePattern.yyyyMMdd, zoneId);
		} else if (MldChtTimeScale.WEEKLY.equals(input.getTimeScale())) {
			putItems(items, fromDate, toDate, WEEK, DatePattern.YYYYww, zoneId);
		} else if (MldChtTimeScale.MONTHLY.equals(input.getTimeScale())) {
			putItems(items, fromDate, toDate, instant -> DateUtils2.plusMonths(instant, 1, zoneId), DatePattern.yyyyMM, zoneId);
		}

		if (MldChtDataGroup.SHOT_COUNT.equals(input.getDataGroup())//
				|| MldChtDataGroup.CYCLE_TIME.equals(input.getDataGroup())) {
			String octs = OptionUtils.getFieldValue(ConfigCategory.OPTIMAL_CYCLE_TIME, "strategy", "ACT");
			output.setOcts(octs);

			if (!MldChtTimeScale.MINUTELY.equals(input.getTimeScale())//
					&& !MldChtTimeScale.HOURLY.equals(input.getTimeScale())) {
				String fromStr = null;
				String toStr;
				QMoldChartStat table = QMoldChartStat.moldChartStat;
				BooleanBuilder filter = new BooleanBuilder();
				filter.and(table.moldId.eq(input.getMoldId()));
				if (MldChtTimeScale.DAILY.equals(input.getTimeScale())) {
					fromStr = DateUtils2.format(fromDate, DatePattern.yyyyMMdd, zoneId);
					toStr = DateUtils2.format(toDate, DatePattern.yyyyMMdd, zoneId);
					filter.and(table.day.between(fromStr, toStr));
				} else if (MldChtTimeScale.WEEKLY.equals(input.getTimeScale())) {
					fromStr = DateUtils2.format(fromDate, DatePattern.YYYYww, zoneId);
					toStr = DateUtils2.format(toDate, DatePattern.YYYYww, zoneId);
					filter.and(table.week.between(fromStr, toStr));
				} else if (MldChtTimeScale.MONTHLY.equals(input.getTimeScale())) {
					fromStr = DateUtils2.format(fromDate, DatePattern.yyyyMM, zoneId);
					toStr = DateUtils2.format(toDate, DatePattern.yyyyMM, zoneId);
					filter.and(table.month.between(fromStr, toStr));
				}

				if (fromStr != null) {
					String[] lastKey = { null };
					String fromDateStr = DateUtils2.format(fromDate, DatePattern.yyyyMMdd, zoneId);
					Map<String, ChartData> chartDataMap = new HashMap<>();
					OptimalCycleTime oct = MoldUtils.getOptimalCycleTime(mold.getId(), mold.getContractedCycleTime(), fromDateStr);
					double optimalCt = oct.getValue(); // 기준 사이클 타임 (contracted? ) --> 초로 계산
					double baseCycleTime = optimalCt / 10.0;
					BeanUtils.get(MoldChartStatRepository.class).findAll(filter, Sort.by("month", "week", "day")).forEach(stat -> {
						String key = null;
						if (MldChtTimeScale.DAILY.equals(input.getTimeScale())) {
							key = stat.getDay();
						} else if (MldChtTimeScale.WEEKLY.equals(input.getTimeScale())) {
							key = stat.getWeek();
						} else if (MldChtTimeScale.MONTHLY.equals(input.getTimeScale())) {
							key = stat.getMonth();
						}
						lastKey[0] = key;

						if (!items.containsKey(key)) {
							return;
						}
						MldChtItem item = items.get(key);
						item.setSc(stat.getSc());
						item.setCvt(stat.getCvt());
						double ct = ValueUtils.toDouble(stat.getCt(), 0d);
						if (ct > 0) {
							ChartData chartData;
							if (chartDataMap.containsKey(key)) {
								chartData = chartDataMap.get(key);
							} else {
								chartData = new ChartData();
								chartDataMap.put(key, chartData);
							}
							chartData.setData(ValueUtils.toInteger(item.getSc(), 0));
							chartData.setCycleTime(ValueUtils.toDouble(chartData.getCycleTime(), 0d) + ct);
						}
					});
//					chartDataMap.forEach((key, chartData) -> chartData.setCycleTime(chartData.getCycleTime() / chartData.getData()));
					DynamicExportService.updateCycleTimeDataList(new ArrayList<>(chartDataMap.values()), mold, baseCycleTime, null);
					chartDataMap.forEach((key, chartData) -> {
						MldChtItem item = items.get(key);
						item.setOct(chartData.getContractedCycleTime());

						item.setCtIn(chartData.getCycleTimeWithin());
						item.setCtL1(chartData.getCycleTimeL1());
						item.setCtL2(chartData.getCycleTimeL2());
						item.setMinCt(chartData.getMinCycleTime());
						item.setMaxCt(chartData.getMaxCycleTime());

						item.setCtUclL2(chartData.getCycleTimePlusL2());
						item.setCtUclL1(chartData.getCycleTimePlusL1());
						item.setCtLclL1(chartData.getCycleTimeMinusL1());
						item.setCtLclL2(chartData.getCycleTimeMinusL2());
					});
					if (lastKey[0] != null) {
						Instant lastFromDate = null;
						if (MldChtTimeScale.DAILY.equals(input.getTimeScale())) {
							lastFromDate = DateUtils2.toInstant(lastKey[0], DatePattern.yyyyMMdd, zoneId).plus(Duration.ofDays(1));
						} else if (MldChtTimeScale.WEEKLY.equals(input.getTimeScale())) {
							lastFromDate = DateUtils2.toInstant(lastKey[0], DatePattern.YYYYww, zoneId).plus(Duration.ofDays(7));
						} else if (MldChtTimeScale.MONTHLY.equals(input.getTimeScale())) {
							lastFromDate = DateUtils2.toInstant(lastKey[0], DatePattern.yyyyMM, zoneId);
							lastFromDate = DateUtils2.plusMonths(lastFromDate, 1, zoneId);
						}
						if (lastFromDate != null) {
							fromDate = lastFromDate;
						}
					}
				}
			}

			if (fromDate.compareTo(toDate) <= 0) {
				if (!MldChtTimeScale.MINUTELY.equals(input.getTimeScale())) {
					String fromStr = DateUtils2.format(fromDate, DatePattern.yyyyMMdd, zoneId);
					String toStr = DateUtils2.format(toDate, DatePattern.yyyyMMdd, zoneId);

					ChartPayload payload = new ChartPayload();
					payload.setMoldId(input.getMoldId());
					payload.setChartDataType(Arrays.asList(ChartDataType.QUANTITY, ChartDataType.CYCLE_TIME));
					payload.setStartDate(fromStr);
					payload.setEndDate(toStr);
					if (MldChtTimeScale.HOURLY.equals(input.getTimeScale())) {
						payload.setDateViewType(DateViewType.HOUR);
					} else if (MldChtTimeScale.DAILY.equals(input.getTimeScale())) {
						payload.setDateViewType(DateViewType.DAY);
					} else if (MldChtTimeScale.WEEKLY.equals(input.getTimeScale())) {
						payload.setDateViewType(DateViewType.WEEK);
					} else if (MldChtTimeScale.MONTHLY.equals(input.getTimeScale())) {
						payload.setDateViewType(DateViewType.MONTH);
					}
					payload.setSkipResinCodeChangeData(true);
					List<ChartData> list = BeanUtils.get(ChartController.class).getMoldChart(payload);
					list.forEach(data -> {
						if (!items.containsKey(data.getTitle())) {
							return;
						}

						MldChtItem item = items.get(data.getTitle());

						item.setSc(ValueUtils.toLong(data.getData(), 0L));
						item.setCvt(data.getAvgCavities());
						if (!ObjectUtils.isEmpty(data.getPartData())) {
							data.getPartData().forEach(partData -> {
								MldChtItemPart itemPart = new MldChtItemPart();
								itemPart.setSc(ValueUtils.toLong(partData.getShot(), 0L));
								itemPart.setCvt(data.getAvgCavities());
								itemPart.setPart(partData.getPartCode() == null ? null : partData.getPartCode().trim());
								itemPart.setQty(ValueUtils.toLong(partData.getPartProduced(), 0L));
								item.addPart(itemPart);
							});
						}

						item.setOct(data.getContractedCycleTime());

						item.setCtIn(data.getCycleTimeWithin());
						item.setCtL1(data.getCycleTimeL1());
						item.setCtL2(data.getCycleTimeL2());
						item.setMinCt(data.getMinCycleTime());
						item.setMaxCt(data.getMaxCycleTime());

						item.setCtUclL2(data.getCycleTimePlusL2());
						item.setCtUclL1(data.getCycleTimePlusL1());
						item.setCtLclL1(data.getCycleTimeMinusL1());
						item.setCtLclL2(data.getCycleTimeMinusL2());
					});
				} else {
					Instant _fromDate = fromDate.minus(Duration.ofHours(1));
					Instant _toDate = toDate.plus(Duration.ofHours(1));
					String fromStr = DateUtils2.format(_fromDate, DatePattern.yyyyMMddHHmmss, zoneId);
					String toStr = DateUtils2.format(_toDate, DatePattern.yyyyMMddHHmmss, zoneId);

					OptimalCycleTime oct = MoldUtils.getOptimalCycleTime(mold.getId(), mold.getContractedCycleTime(), fromStr);
					double optimalCt = oct.getValue(); // 기준 사이클 타임 (contracted? ) --> 초로 계산
					double baseCycleTime = optimalCt / 10.0;

					ChartPayload payload = new ChartPayload();
					payload.setDateViewType(DateViewType.HOUR);
					payload.setMoldId(input.getMoldId());
					List<String> minMaxTitles = Arrays.asList(fromStr.substring(0, 10), toStr.substring(0, 10));
					Map<String, AvgCavityStatisticsData> cavities = BeanUtils.get(MoldRepositoryImpl.class)//
							.getCavitiesByTitles(payload, minMaxTitles);
					Map<String, PartShotData> shots = BeanUtils.get(MoldRepositoryImpl.class).getPartShotList(payload, minMaxTitles).stream()//
							.filter(shot -> !ObjectUtils.isEmpty(shot.getTitle()))//
							.collect(Collectors.toMap(shot -> shot.getTitle(), shot -> shot));

					QDataCounter table = QDataCounter.dataCounter;
					BooleanBuilder filter = new BooleanBuilder();
					filter.and(table.counterId.eq(mold.getCounterCode()));
					filter.and(new BooleanBuilder()//
							.or(table.shotStartTime.goe(fromStr).and(table.shotEndTime.loe(toStr)))//
							.or(table.shotStartTime.loe(fromStr).and(table.shotEndTime.goe(toStr)))//
							.or(table.shotStartTime.loe(fromStr).and(table.shotStartTime.goe(toStr)))//
							.or(table.shotEndTime.loe(fromStr).and(table.shotEndTime.goe(toStr)))//
					);

					BeanUtils.get(DataCounterRepository.class).findAll(filter, Sort.by("shotStartTime")).forEach(data -> {
						if (ObjectUtils.isEmpty(data.getCycleTimes())) {
							return;
						}
						Instant since = DateUtils2.toInstant(data.getShotStartTime(), DatePattern.yyyyMMddHHmmss, zoneId);

						Map<String, ChartData> chartDataMap = new HashMap<>();
						for (CycleTime cycleTime : data.getCycleTimes()) {
							String key = DateUtils2.format(since, DatePattern.yyyyMMddHHmm, zoneId);
							double ct = cycleTime == null ? 0d : cycleTime.getCycleTime().doubleValue();

							if (items.containsKey(key)) {
								MldChtItem item = items.get(key);

								item.setSc(ValueUtils.toLong(item.getSc(), 0L) + 1);
								String hour = key.substring(0, 10);
								if (item.getCvt() == null) {
									item.setCvt(cavities.containsKey(hour) ? cavities.get(hour).getAvgCavity() : 1d);
								}
								if (shots.containsKey(hour)) {
									PartShotData partData = shots.get(hour);
									String partCode = partData.getPartCode() == null ? null : partData.getPartCode().trim();
									if (ObjectUtils.isEmpty(partCode)) {
										return;
									}
									MldChtItemPart itemPart = null;
									if (!ObjectUtils.isEmpty(item.getParts())) {
										for (MldChtItemPart prevItemPart : item.getParts()) {
											if (partCode.equals(prevItemPart.getPart())) {
												itemPart = prevItemPart;
												break;
											}
										}
									}
									if (itemPart == null) {
										itemPart = new MldChtItemPart();
										itemPart.setPart(partCode);
										itemPart.setCvt(item.getCvt());
										item.addPart(itemPart);
									}
									itemPart.setSc(item.getSc());
									itemPart.setQty(item.getQty());
								}

								item.setOct(baseCycleTime);
								if (ct > 0) {
									ChartData chartData;
									if (chartDataMap.containsKey(key)) {
										chartData = chartDataMap.get(key);
									} else {
										chartData = new ChartData();
										chartDataMap.put(key, chartData);
									}
									chartData.setData(ValueUtils.toInteger(item.getSc(), 0));
									chartData.setCycleTime(ValueUtils.toDouble(chartData.getCycleTime(), 0d) + ct);
								}
							}

							if (ct > 0) {
								since = since.plus(Duration.ofMillis(ValueUtils.toLong(ct * 1000, 0L)));
							}
						}

						// CycleTime
						chartDataMap.forEach((key, chartData) -> chartData.setCycleTime(chartData.getCycleTime() / chartData.getData()));
						DynamicExportService.updateCycleTimeDataList(new ArrayList<>(chartDataMap.values()), mold, baseCycleTime, null);
						chartDataMap.forEach((key, chartData) -> {
							MldChtItem item = items.get(key);
							item.setOct(chartData.getContractedCycleTime());

							item.setCtIn(nullable(chartData.getCycleTimeWithin()));
							item.setCtL1(nullable(chartData.getCycleTimeL1()));
							item.setCtL2(nullable(chartData.getCycleTimeL2()));
							item.setMinCt(nullable(chartData.getMinCycleTime()));
							item.setMaxCt(nullable(chartData.getMaxCycleTime()));

							item.setCtUclL2(nullable(chartData.getCycleTimePlusL2()));
							item.setCtUclL1(nullable(chartData.getCycleTimePlusL1()));
							item.setCtLclL1(nullable(chartData.getCycleTimeMinusL1()));
							item.setCtLclL2(nullable(chartData.getCycleTimeMinusL2()));
						});
					});
				}
			}
		}

		items.forEach((title, item) -> {
			item.setSc(notNull(item.getSc()));
			item.setOct(ValueUtils.toDouble(item.getOct(), mold.getContractedCycleTimeSeconds()));
			output.add(item);
		});
		return output;
	}

	private static Double nullable(Double value) {
		return value == null || value.equals(0d) ? null : value;
	}

//	private static Double notNull(Double value) {
//		return value == null ? 0d : value;
//	}

//	private static Long nullable(Long value) {
//		return value == null || value.equals(0L) ? null : value;
//	}

	private static Long notNull(Long value) {
		return value == null ? 0L : value;
	}

	private static void putItems(Map<String, MldChtItem> items, Instant fromDate, Instant toDate, int interval, String pattern, String zoneId) {
		putItems(items, fromDate, toDate, instant -> instant.plusSeconds(interval), pattern, zoneId);
	}

	private static void putItems(Map<String, MldChtItem> items, Instant fromDate, Instant toDate, Closure1Param<Instant, Instant> closure, String pattern, String zoneId) {
		String from = DateUtils2.format(fromDate, pattern, zoneId);
		String to = DateUtils2.format(toDate, pattern, zoneId);
		String key = from;
		String prevKey = null;
		while (to.compareTo(key) > 0) {
			MldChtItem item = new MldChtItem();

			if (key.length() == 12) {
//				String year = key.substring(0, 4);
				String month = key.substring(4, 6);
				String day = key.substring(6, 8);
				String hour = key.substring(8, 10);
				String title = key.substring(10);
				item.setGroup(month + "-" + day + " " + hour);
				item.setTitle(Integer.parseInt(title) + "");
			} else if (key.length() == 10) {
				String year = key.substring(0, 4);
				String month = key.substring(4, 6);
				String day = key.substring(6, 8);
				String title = key.substring(8);
				item.setGroup(year + "-" + month + "-" + day);
				item.setTitle(Integer.parseInt(title) + "");
			} else if (key.length() == 8) {
				String year = key.substring(0, 4);
				String month = key.substring(4, 6);
				String title = key.substring(6);
				item.setGroup(year + "-" + month);
				item.setTitle(Integer.parseInt(title) + "");
			} else if (key.length() == 6) {
				String year = key.substring(0, 4);
				String title = key.substring(4);
				item.setGroup(year);
				item.setTitle(Integer.parseInt(title) + "");
			} else {
				item.setTitle(key);
			}

			items.put(key, item);

			Instant current = DateUtils2.toInstant(key, pattern, zoneId);
			item.setTime(DateUtils2.format(current, DatePattern.yyyyMMddHHmm, zoneId));
			Instant next = closure.execute(current);
			key = DateUtils2.format(next, pattern, zoneId);
			// TODO Remove after finding the exact problem
			while (prevKey != null && pattern.contains("w") && prevKey.equals(key)) {
				next = next.plusSeconds(DAY);
				key = DateUtils2.format(next, pattern, zoneId);
			}
			prevKey = key;
		}
	}

	private static Instant adjustDate(String value, String fieldName, String fromTo, String zoneId) {
		LogicUtils.assertNotEmpty(value, fieldName);

		int len = value.length();
		boolean from = !"TO".equals(fromTo);

		Instant instant;
		if (len == 14) {
			instant = from ? DateUtils2.toInstant(value, DatePattern.yyyyMMddHHmmss, zoneId) : DateUtils2.toInstant(value + "999", DatePattern.yyyyMMddHHmmssSSS, zoneId);
		} else if (len == 12) {
			instant = from ? DateUtils2.toInstant(value, DatePattern.yyyyMMddHHmm, zoneId) : DateUtils2.toInstant(value + "59999", DatePattern.yyyyMMddHHmmssSSS, zoneId);
		} else if (len == 10) {
			instant = from ? DateUtils2.toInstant(value, DatePattern.yyyyMMddHH, zoneId) : DateUtils2.toInstant(value + "5959999", DatePattern.yyyyMMddHHmmssSSS, zoneId);
		} else if (len == 8) {
			instant = from ? DateUtils2.toInstant(value, DatePattern.yyyyMMdd, zoneId) : DateUtils2.toInstant(value + "235959999", DatePattern.yyyyMMddHHmmssSSS, zoneId);
		} else if (len == 6) {
			instant = DateUtils2.toInstant(value, DatePattern.yyyyMM, zoneId);
			if (!from) {
				instant = DateUtils2.plusMonths(instant, 1, zoneId);
				instant = instant.minusMillis(1);
			}
		} else if (len == 4) {
			instant = from ? DateUtils2.toInstant(value, DatePattern.yyyy, zoneId) : DateUtils2.toInstant(value + "1231235959999", DatePattern.yyyyMMddHHmmssSSS, zoneId);
		} else {
			throw new LogicException("INVALID_DATE_FORMAT", "Invalid " + fieldName + ": " + value, new Property(fieldName, value));
		}

//		if (to) {
//			instant = instant.plusSeconds(1);
//		}
		return instant;
	}

	public MldChtGetOptionsOut getOptions(MldChtGetOptionsIn input) {
		/**
		 * 1. Validation
		 */

		ValueUtils.assertNotEmpty(input.getMoldId(), "moldId");
		ValueUtils.assertNotEmpty(input.getDataGroup(), "dataGroup");

		Mold mold = BeanUtils.get(MoldRepository.class).findById(input.getMoldId()).orElseThrow(() -> DataUtils.newDataNotFoundException(Mold.class, "mold_id", input.getMoldId()));

		MldChtGetOptionsOut output = new MldChtGetOptionsOut();

		Counter counter = mold.getCounter();
		if (counter == null || ObjectUtils.isEmpty(counter.getEquipmentCode())) {
			output.setAvailableTimeScales(Arrays.asList(MldChtTimeScale.DAILY, MldChtTimeScale.WEEKLY, MldChtTimeScale.MONTHLY));
		} else if (counter.getEquipmentCode().startsWith("EM") || counter.getEquipmentCode().startsWith("SC_")) {
			output.setAvailableTimeScales(Arrays.asList(MldChtTimeScale.MINUTELY, MldChtTimeScale.HOURLY, MldChtTimeScale.DAILY, MldChtTimeScale.WEEKLY, MldChtTimeScale.MONTHLY));
		} else if (counter.getEquipmentCode().startsWith("NCM")) {
			output.setAvailableTimeScales(Arrays.asList(MldChtTimeScale.HOURLY, MldChtTimeScale.DAILY, MldChtTimeScale.WEEKLY, MldChtTimeScale.MONTHLY));
		} else {
			output.setAvailableTimeScales(Arrays.asList(MldChtTimeScale.DAILY, MldChtTimeScale.WEEKLY, MldChtTimeScale.MONTHLY));
		}

		return output;
	}
}
