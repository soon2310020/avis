package com.emoldino.api.supplychain.resource.base.product.service.moldstat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.common.resource.base.masterdata.repository.mold2.Mold2Repository;
import com.emoldino.api.supplychain.resource.base.product.repository.prodmoldstat.ProdMoldStat;
import com.emoldino.api.supplychain.resource.base.product.repository.prodmoldstat.ProdMoldStatRepository;
import com.emoldino.api.supplychain.resource.base.product.repository.prodmoldstat.QProdMoldStat;
import com.emoldino.api.supplychain.resource.base.product.util.ProductUtils;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.api.statistics.StatisticsPartRepository;

@Service
public class ProdMoldStatService {

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StatSum {
		private Long actualSc;
		private Double avgCt;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StatPart {
		private Long projectId;
		private Long partId;
		private Integer cavity;
	}

	@Transactional(propagation = Propagation.NEVER)
	public void summarizeBatch() {
		JobUtils.runIfNotRunning("sc.prodmold.summarizeBatch", new JobOptions().setClustered(true), () -> {
			_summarizeBatch();
		});
	}

	@Async
	@Transactional(propagation = Propagation.NEVER)
	public void cleanBatch() {
		JobUtils.runIfNotRunning("sc.prodmold.summarizeBatch", new JobOptions().setClustered(true), () -> {
			DataUtils.runContentBatch(//
					ProdMoldStatRepository.class, //
					null, //
					Sort.by("id"), 1000, false, //
					content -> TranUtils.doNewTran(() -> BeanUtils.get(ProdMoldStatRepository.class).deleteAll(content))//
			);
			_summarizeBatch();
		});
	}

	private void _summarizeBatch() {

		String minWeek = "202001";
//		DataUtils.runBatch(//
//				ProdMoldStatRepository.class, //
//				new BooleanBuilder().and(Q.prodMoldStat.week.lt(minWeek)), Sort.by("id"), 100, false, stat -> {
//					TranUtils.doNewTran(() -> BeanUtils.get(ProdMoldStatRepository.class).delete(stat));
//				}//
//		);

		String thisWeek = ProductUtils.getThisWeek();
		DataUtils.runBatch(//
				Mold2Repository.class, //
				null, Sort.by("id"), 100, true, mold -> {
					QProdMoldStat table = QProdMoldStat.prodMoldStat;

					Long moldId = mold.getId();
					Long supplierId = mold.getSupplierCompanyId();
					// Approved Cycle Time
					Integer apprCt = mold.getContractedCycleTime();
					// Ideal Shot Count
					Long idealSc = apprCt == null || apprCt.equals(0) ? 0L : ValueUtils.toLong(3600 / apprCt, 0L);
					// Production Hours per Day
					Integer prodHoursPerDay = ValueUtils.toInteger(mold.getShiftsPerDay(), null);
					// Production Days per Week
					Integer prodDays = ValueUtils.toInteger(mold.getProductionDays(), null);
					// Target Uptime Rate
					Integer targetUptimeRate = mold.getUptimeTarget();

					ProdMoldStat[] lastStat = { null };
					String[] _week = { null };
					TranUtils.doNewTran(() -> {
						BeanUtils.get(ProdMoldStatRepository.class).findAll(table.moldId.eq(moldId), PageRequest.of(0, 1, Direction.DESC, "week"))
								.forEach(stat -> lastStat[0] = stat);
						if (lastStat[0] != null) {
							_week[0] = lastStat[0].getWeek();
						} else {
							_week[0] = BeanUtils.get(JPAQueryFactory.class)//
									.select(Q.statistics.week)//
									.from(Q.statistics)//
									.innerJoin(Q.statisticsPart).on(Q.statisticsPart.statisticsId.eq(Q.statistics.id))//
									.where(Q.statistics.moldId.eq(moldId)//
											.and(Q.statistics.week.goe(minWeek))//
											.and(Q.statistics.week.lt(thisWeek)))//
									.orderBy(Q.statistics.week.asc())//
									.limit(1L)//
									.fetchOne();
						}
					});

					int counter = 0;
					while (counter++ < 10000 && _week[0] != null && _week[0].compareTo(thisWeek) < 0) {
						TranUtils.doNewTran(() -> {
							String week = _week[0];
							_week[0] = DateUtils2.plus(_week[0], DatePattern.YYYYww, Duration.ofDays(7));

							List<StatPart> parts = new ArrayList<>();
							Long statId = BeanUtils.get(JPAQueryFactory.class)//
									.select(Q.statistics.id)//
									.from(Q.statistics)//
									.innerJoin(Q.statisticsPart).on(Q.statisticsPart.statisticsId.eq(Q.statistics.id))//
									.where(Q.statistics.moldId.eq(moldId)////
											.and(Q.statistics.week.goe(minWeek))//
											.and(Q.statistics.week.loe(week)))
									.orderBy(Q.statistics.week.desc())//
									.limit(1L)//
									.fetchOne();
							if (statId == null) {
								return;
							}
							BeanUtils.get(StatisticsPartRepository.class).findByStatisticsId(statId).forEach(part -> {
								if (part.getPartId() == null) {
									return;
								}
								parts.add(ValueUtils.map(part, StatPart.class));
							});
							if (ObjectUtils.isEmpty(parts)) {
								return;
							}

							String month = DateUtils2.toMonthByWeek(week);
							// Weighted Average Cycle Time
							double wact = MoldUtils.getWact(mold.getId(), apprCt, month);

							// Actual Shot Count
							Long _actualSc = null;
							// Average Cycle Time
							Double _avgCt = null;
							if (idealSc > 0) {
								JPQLQuery<StatSum> query = BeanUtils.get(JPAQueryFactory.class)//
										.select(Projections.constructor(StatSum.class, //
												Q.statistics.shotCount.longValue().sum().coalesce(0L), //
												Q.statistics.ctVal.multiply(Q.statistics.shotCount).sum()//
														.divide(Q.statistics.shotCount.sum()).coalesce(0d)))//
										.from(Q.statistics)//
										.where(Q.statistics.moldId.eq(moldId)//
												.and(Q.statistics.week.eq(week))//
												.and(Q.statistics.shotCount.gt(0.5 * idealSc))//
												.and(Q.statistics.ct.gt(0))//
												.and(Q.statistics.ct.lt(5000)));
								StatSum sum = query.fetchOne();
								_actualSc = sum.getActualSc();
								_avgCt = sum.getAvgCt();
							}
							if (_avgCt == null || _avgCt <= 0) {
								JPQLQuery<StatSum> query = BeanUtils.get(JPAQueryFactory.class)//
										.select(Projections.constructor(StatSum.class, //
												Q.statistics.shotCount.longValue().sum().coalesce(0L), //
												Q.statistics.ctVal.multiply(Q.statistics.shotCount).sum()//
														.divide(Q.statistics.shotCount.sum()).coalesce(0d)))//
										.from(Q.statistics)//
										.where(Q.statistics.moldId.eq(moldId)//
												.and(Q.statistics.week.eq(week))//
												.and(Q.statistics.shotCount.gt(2))//
												.and(Q.statistics.ct.gt(0))//
												.and(Q.statistics.ct.lt(5000)));
								StatSum sum = query.fetchOne();
								_actualSc = sum.getActualSc();
								_avgCt = sum.getAvgCt();
							}
							if (_avgCt == null || _avgCt <= 0) {
								_actualSc = 0L;
								_avgCt = lastStat == null || lastStat[0] == null ? ValueUtils.toDouble(apprCt, 0d) : lastStat[0].getAvgCycleTime();
							}
							Long actualSc = _actualSc;
							Long recentSc = actualSc > 0L || lastStat[0] == null ? actualSc : lastStat[0].getRecentShotCount();
							Double avgCt = _avgCt;

							for (StatPart part : parts) {
								Long productId = part.getProjectId();
								Long partId = part.getPartId();
								Integer cavityCount = part.getCavity();

								// Weekly Capacity
								Long weeklyCapa = null;
								if (wact > 0d) {
									weeklyCapa = ProductUtils.calcWeeklyCapa(avgCt, cavityCount, prodHoursPerDay, prodDays, targetUptimeRate);
								}

								ProdMoldStat stat = BeanUtils.get(ProdMoldStatRepository.class)
										.findOne(table.moldId.eq(moldId).and(table.partId.eq(partId)).and(table.week.eq(week))).orElse(new ProdMoldStat());
								stat.setMoldId(moldId);
								stat.setProductId(productId);
								stat.setPartId(partId);
								stat.setSupplierId(supplierId);
								stat.setWeek(week);
								stat.setApprCycleTime(apprCt);
								stat.setWeightedAvgCycleTime(wact);
								stat.setIdealShotCount(idealSc);
								stat.setActualShotCount(actualSc);
								stat.setRecentShotCount(recentSc);
								stat.setAvgCycleTime(avgCt);
								stat.setProdHoursPerDay(prodHoursPerDay);
								stat.setProdDays(prodDays);
								stat.setTargetUptimeRate(targetUptimeRate);
								stat.setCavityCount(cavityCount);
								stat.setWeeklyCapa(weeklyCapa);
								BeanUtils.get(ProdMoldStatRepository.class).save(stat);

								lastStat[0] = stat;
							}
						});
					}
				}//
		);
	}

}
