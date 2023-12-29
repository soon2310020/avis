package com.emoldino.api.analysis.resource.base.data.service.moldchartstat;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.base.data.repository.moldchartstat.MoldChartStat;
import com.emoldino.api.analysis.resource.base.data.repository.moldchartstat.MoldChartStatRepository;
import com.emoldino.api.analysis.resource.base.data.repository.moldchartstat.QMoldChartStat;
import com.emoldino.api.analysis.resource.composite.mldcht.dto.get.MldChtGetOut.MldChtItemPart;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.api.chart.ChartController;
import saleson.api.chart.payload.ChartPayload;
import saleson.api.mold.MoldRepository;
import saleson.api.statistics.StatisticsRepository;
import saleson.common.enumeration.ChartDataType;
import saleson.common.enumeration.DateViewType;
import saleson.model.Mold;
import saleson.model.QStatistics;
import saleson.model.Statistics;

@Service
public class MoldChartStatService {

	public void summarizeBatch() {
		JobUtils.runIfNotRunning("sc.moldChart.summarizeBatch", new JobOptions().setClustered(true), () -> {
			int pageNo = 0;
			Page<Mold> moldPage;
			QMoldChartStat table = QMoldChartStat.moldChartStat;
			while (!(moldPage = BeanUtils.get(MoldRepository.class).findAll(new BooleanBuilder(), PageRequest.of(pageNo++, 100, Sort.by("id")))).isEmpty()) {
				moldPage.forEach(mold -> {
					int day = ValueUtils.toInteger(DateUtils2.format(DateUtils2.getInstant(), DatePattern.dd, Zone.SYS), 0);
					int minus = day >= 15 ? 0 : -1;
					Instant lessThan = minus >= 0 ? DateUtils2.getInstant() : DateUtils2.plusMonths(DateUtils2.getInstant(), minus, Zone.SYS);
					// Monthly
					{
						String lessStr = DateUtils2.format(lessThan, DatePattern.yyyyMM, Zone.SYS);

						Page<MoldChartStat> page = BeanUtils.get(MoldChartStatRepository.class).findAll(//
								new BooleanBuilder()//
										.and(table.timeScale.eq("MONTHLY"))//
										.and(table.moldId.eq(mold.getId()))//
										.and(table.month.loe(lessStr)), //
								PageRequest.of(0, 1, Direction.DESC, "month"));
						String overStr;
						if (page.isEmpty()) {
							QStatistics stat = QStatistics.statistics;
							Page<Statistics> statPage = BeanUtils.get(StatisticsRepository.class).findAll(//
									new BooleanBuilder()//
											.and(stat.moldId.eq(mold.getId()))//
											.and(stat.year.goe("2015")),
									PageRequest.of(0, 1, Direction.ASC, "month"));
							if (statPage.isEmpty()) {
								return;
							}
							overStr = ValueUtils.toInteger(statPage.getContent().get(0).getMonth(), 0) - 1 + "";
						} else {
							overStr = page.getContent().get(0).getMonth();
						}

						Instant since;
						try {
							since = DateUtils2.toInstant(overStr, DatePattern.yyyyMM, Zone.SYS);
						} catch (DateTimeParseException e) {
							if (NumberUtils.isCreatable(overStr) && overStr.length() == 6 && overStr.endsWith("00")) {
								overStr = overStr.substring(0, 5) + "1";
								since = DateUtils2.toInstant(overStr, DatePattern.yyyyMM, Zone.SYS);
							} else {
								throw e;
							}
						}
						String _overStr = overStr;
						String year = DateUtils2.format(since, DatePattern.yyyy, Zone.SYS);
						do {
							ChartPayload payload = new ChartPayload();
							payload.setDateViewType(DateViewType.MONTH);
							payload.setChartDataType(Arrays.asList(ChartDataType.QUANTITY, ChartDataType.CYCLE_TIME));
							payload.setMoldId(mold.getId());
							payload.setYear(year);
							TranUtils.doNewTran(() -> {
								List<MoldChartStat> list = new ArrayList<>();
								BeanUtils.get(ChartController.class).getMoldChart(payload).forEach(chartData -> {
									String month = chartData.getTitle();
									String _year = month.substring(0, 4);
									if (_overStr.compareTo(month) >= 0 || lessStr.compareTo(month) <= 0) {
										return;
									}
									MoldChartStat item = new MoldChartStat();
									item.setMoldId(mold.getId());
									item.setTimeScale("MONTHLY");
									item.setYear(_year);
									item.setMonth(month);
									item.setSc(ValueUtils.toLong(chartData.getData(), 0L));
									item.setCvt(chartData.getAvgCavities());
									item.setCt(chartData.getCycleTime());
									if (!ObjectUtils.isEmpty(chartData.getPartData())) {
										chartData.getPartData().forEach(partData -> {
											MldChtItemPart itemPart = new MldChtItemPart();
											itemPart.setSc(ValueUtils.toLong(partData.getShot(), 0L));
											itemPart.setCvt(chartData.getAvgCavities());
											itemPart.setPart(partData.getPartCode());
											itemPart.setQty(ValueUtils.toLong(partData.getPartProduced(), 0L));
											item.addPart(itemPart);
										});
									}
									list.add(item);
								});
								BeanUtils.get(MoldChartStatRepository.class).saveAll(list);
							});
							year = ValueUtils.toInteger(year, null) + 1 + "";
						} while (lessStr.compareTo(year) > 0);
					}
					// Weekly
					{
						String lessStr = DateUtils2.format(lessThan, DatePattern.YYYYww, Zone.SYS);

						Page<MoldChartStat> page = BeanUtils.get(MoldChartStatRepository.class).findAll(//
								new BooleanBuilder()//
										.and(table.timeScale.eq("WEEKLY"))//
										.and(table.moldId.eq(mold.getId()))//
										.and(table.week.loe(lessStr)), //
								PageRequest.of(0, 1, Direction.DESC, "week"));
						String overStr;
						if (page.isEmpty()) {
							QStatistics stat = QStatistics.statistics;
							Page<Statistics> statPage = BeanUtils.get(StatisticsRepository.class).findAll(//
									new BooleanBuilder()//
											.and(stat.moldId.eq(mold.getId()))//
											.and(stat.year.goe("2015")),
									PageRequest.of(0, 1, Direction.ASC, "week"));
							if (statPage.isEmpty()) {
								return;
							}
							overStr = statPage.getContent().get(0).getWeek();
						} else {
							overStr = page.getContent().get(0).getWeek();
						}

						Instant since = DateUtils2.toInstant(overStr, DatePattern.YYYYww, Zone.SYS);
						String year = DateUtils2.format(since, DatePattern.yyyy, Zone.SYS);
						do {
							ChartPayload payload = new ChartPayload();
							payload.setDateViewType(DateViewType.WEEK);
							payload.setChartDataType(Arrays.asList(ChartDataType.QUANTITY, ChartDataType.CYCLE_TIME));
							payload.setMoldId(mold.getId());
							payload.setYear(year);
							TranUtils.doNewTran(() -> {
								List<MoldChartStat> list = new ArrayList<>();
								BeanUtils.get(ChartController.class).getMoldChart(payload).forEach(chartData -> {
									String week = chartData.getTitle();
									String _year = week.substring(0, 4);
									if (overStr.compareTo(week) >= 0 || lessStr.compareTo(week) <= 0) {
										return;
									}
									MoldChartStat item = new MoldChartStat();
									item.setMoldId(mold.getId());
									item.setTimeScale("WEEKLY");
									item.setYear(_year);
									item.setWeek(week);
									item.setSc(ValueUtils.toLong(chartData.getData(), 0L));
									item.setCvt(chartData.getAvgCavities());
									item.setCt(chartData.getCycleTime());
									if (!ObjectUtils.isEmpty(chartData.getPartData())) {
										chartData.getPartData().forEach(partData -> {
											MldChtItemPart itemPart = new MldChtItemPart();
											itemPart.setSc(ValueUtils.toLong(partData.getShot(), 0L));
											itemPart.setCvt(chartData.getAvgCavities());
											itemPart.setPart(partData.getPartCode());
											itemPart.setQty(ValueUtils.toLong(partData.getPartProduced(), 0L));
											item.addPart(itemPart);
										});
									}
									list.add(item);
								});
								BeanUtils.get(MoldChartStatRepository.class).saveAll(list);
							});
							year = ValueUtils.toInteger(year, null) + 1 + "";
						} while (lessStr.compareTo(year) > 0);
					}
					// Daily
					{

					}
				});
			}

//			QStatistics table = QStatistics.statistics;
//			long[] lastId = { 0 };
//			int pageNo = 0;
//			Page<Statistics> page;
//			while (!(page = BeanUtils.get(StatisticsRepository.class).findAll(new BooleanBuilder()//
//					.and(table.id.gt(lastId[0])), //
//					PageRequest.of(pageNo++, 1000, Direction.ASC, "id"))//
//			).isEmpty()) {
//				page.forEach(stat -> {
//					lastId[0] = stat.getId();
//
//					TranUtils.doNewTran(() -> {
////						MoldChartStatAck ack = getAck(stat.getId());
////						if (ack != null && "O".equals(ack.getProcStatus())) {
////							return;
////						}
////
////						if (ack == null) {
////							ack = new MoldChartStatAck();
////							ack.setDataType("STATISTICS");
////							ack.setDataId(stat.getId());
////							DataUtils.populate4Insert(ack);
////						} else {
////							DataUtils.populate4Update(ack);
////						}
////
////						ack.setProcStatus("O");
////						BeanUtils.get(MoldChartStatAckRepository.class).save(ack);
//
////						Long machineId = getMachineId(stat);
//
//						getParts(stat.getId()).forEach(part -> {
//							// Minutely
//
//							// Hourly
//
//							// Daily
//
//							// Weekly
//
//							// Monthly
//
//							MoldChartStat chart = new MoldChartStat();
//							chart.setMoldId(stat.getMoldId());
////							chart.setPartId(part.getPartId());
////							chart.setMachineId(machineId);
//
////							private String timeScale;
////							private String year;
////							private String month;
////							private String week;
////							private String day;
////							private String hour;
////							private String minute;
//
////							private Double act;
////							private Double wact;
//
////							if (stat.getShotCount() != null && stat.getShotCount() > 0L) {
////								chart.setSc(ValueUtils.toLong(chart.getSc(), 0L) + stat.getShotCount());
////								if (part.getCavity() != null && part.getCavity() > 0) {
////									chart.setTotalCvt(chart.getTotalCvt() + (part.getCavity() * stat.getShotCount()));
////								}
////							}
////
////							private Double ct;
////							private Double minCt;
////							private Double maxCt;
////							private Double llct;
////							private Double ulct;
////
////							private Long scVal;
////							private Double ctVal;
////							private Double minCtVal;
////							private Double maxCtVal;
////							private Double llctVal;
////							private Double ulctVal;
////
////							private Integer tav;
////							private Integer tlo;
////							private Integer thi;
////
////							private Long uptime;
//						});
//					});
//				});
//			}
////			int i = 0;
////			Page<Category> page;
////			QCategory table = QCategory.category;
////			BooleanBuilder builder = new BooleanBuilder();
////			builder.and(table.level.eq(1));
////			builder.and(table.enabled.eq(true));
////			while (!(page = BeanUtils.get(CategoryService.class).findAll(builder, PageRequest.of(i++, 10, Direction.ASC, "id"))).isEmpty()) {
////				page.forEach(item -> summarizeCategory(item));
////			}
		});
	}

//	private MoldChartStatAck getAck(Long statId) {
//		QMoldChartStatAck table = QMoldChartStatAck.moldChartStatAck;
//		BooleanBuilder filter = new BooleanBuilder();
//		filter.and(table.dataType.eq("STATISTICS"));
//		filter.and(table.dataId.eq(statId));
//		MoldChartStatAck ack = BeanUtils.get(MoldChartStatAckRepository.class).findOne(filter).orElse(null);
//		return ack;
//	}

//	private Iterable<StatisticsPart> getParts(Long statId) {
//		QStatisticsPart table = QStatisticsPart.statisticsPart;
//		BooleanBuilder filter = new BooleanBuilder();
//		filter.and(table.statisticsId.eq(statId));
//		return BeanUtils.get(StatisticsPartRepository.class).findAll(filter);
//	}
//
//	private Long getMachineId(Statistics stat) {
//		return null;
//	}

}
