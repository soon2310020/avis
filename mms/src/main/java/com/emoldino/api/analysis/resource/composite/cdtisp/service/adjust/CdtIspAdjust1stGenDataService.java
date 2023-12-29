package com.emoldino.api.analysis.resource.composite.cdtisp.service.adjust;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.composite.cdtisp.dto.CdtIspAdjustIn;
import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.CacheDataUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.api.mold.MoldRepository;
import saleson.api.statistics.StatisticsRepository;
import saleson.model.Cdata;
import saleson.model.Mold;
import saleson.model.QLogTransfer;
import saleson.model.Statistics;
import saleson.model.Transfer;
import saleson.service.transfer.CdataRepository;
import saleson.service.transfer.LogTransferRepository;
import saleson.service.transfer.TransferController;
import saleson.service.transfer.TransferService;

@Service
public class CdtIspAdjust1stGenDataService {

	@Async
	@Transactional(propagation = Propagation.NEVER)
	public void adjustBatch(CdtIspAdjustIn input) {
		JobUtils.runIfNotRunning("CdtIsp.adjust1stGenDataBatch", new JobOptions().setClustered(true), () -> {
			String lastDate = ObjectUtils.isEmpty(input.getCi()) ? //
					ValueUtils.toString(CacheDataUtils.get("CdtIspAdjust1stGenData", "lastDate", String.class), "20220731") //
					: ValueUtils.toString(input.getLastDate(), "20220731");
			String date = DateUtils2.format(DateUtils2.getInstant().minus(Duration.ofDays(1L)), DatePattern.yyyyMMdd, Zone.GMT);
			while ((lastDate = DateUtils2.plus(lastDate, DatePattern.yyyyMMdd, Duration.ofDays(1L))).compareTo(date) < 0) {
				String _lastDate = lastDate;
				DataUtils.runBatch(MoldRepository.class, //
						new BooleanBuilder().and(ObjectUtils.isEmpty(input.getCi()) ? //
								Q.mold.counter.equipmentCode.startsWith("CMS")//
								: Q.mold.counter.equipmentCode.in(input.getCi())), //
						Sort.by("id"), 100, true, //
						mold -> {
//							JobUtils.runIfNotRunning("CdtIsp.adjust." + mold.getEquipmentCode() + "." + _lastDate, new JobOptions().setClustered(true), () -> {
							adjust(mold, _lastDate);
//							});
						});

				if (ObjectUtils.isEmpty(input.getCi())) {
					CacheDataUtils.save("CdtIspAdjust1stGenData", "lastDate", lastDate);
				}
			}
		});
	}

	private void adjust(Mold mold, String date) {
		Instant inst = DateUtils2.toInstant(date, DatePattern.yyyyMMdd, Zone.GMT);
		Instant nextInst = inst.plus(Duration.ofDays(1L));
		BooleanBuilder filter = new BooleanBuilder()//
				.and(Q.cdata.moldId.eq(mold.getId()))//
				.and(Q.cdata.createdAt.goe(inst))//
				.and(Q.cdata.createdAt.lt(nextInst))//
				.and(Q.cdata.day.goe("20150101"))//
				.and(Q.cdata.sc.isNotNull())//
				.and(Q.cdata.sc.gt(0))//
				.and(Q.cdata.sc.lt(999999000))//
				.and(Q.cdata.ct.isNotNull())//
				.and(Q.cdata.ct.gt(50))//
				.and(Q.cdata.ct.lt(6000))//
				.and(Q.cdata.lst.isNotNull())//
				.and(Q.cdata.lst.isNotEmpty());

		DataUtils.runBatch(BeanUtils.get(JPAQueryFactory.class)//
				.selectFrom(Q.cdata)//
				.leftJoin(Q.statistics).on(Q.statistics.cdataId.eq(Q.cdata.id))//
				.where(filter.and(Q.statistics.id.isNull()))//
				.orderBy(Q.cdata.id.asc()), //
				100, true, //
//		DataUtils.runBatch(CdataRepository.class, //
//				filter, //
//				Sort.by("id"), 100, true, //
				cdata -> {
					TranUtils.doNewTran(() -> {
						if (BeanUtils.get(StatisticsRepository.class).exists(Q.statistics.cdataId.eq(cdata.getId()))) {
							return;
						}

						int totalFsc;
						{
							Cdata prevCdata;
							{
								Cdata[] prevCdatas = { null };
								BeanUtils.get(CdataRepository.class).findAll(new BooleanBuilder()//
										.and(Q.cdata.moldId.eq(mold.getId()))//
										.and(Q.cdata.sn.lt(cdata.getSn()))//
										.and(Q.cdata.day.goe("20150101"))//
										.and(Q.cdata.sc.isNotNull())//
										.and(Q.cdata.sc.lt(999999000))//
										.and(Q.cdata.sc.ne(cdata.getSc()))//
										.and(Q.cdata.ct.isNotNull())//
										.and(Q.cdata.ct.gt(0))//
										.and(Q.cdata.lst.isNotNull())//
										.and(Q.cdata.lst.isNotEmpty())//
										.and(Q.cdata.lst.lt(cdata.getLst())), //
										PageRequest.of(0, 1, Direction.DESC, "sn"))//
										.forEach(item -> prevCdatas[0] = item);
								prevCdata = prevCdatas[0];
							}
							if (prevCdata.getSc() > cdata.getSc()) {
								return;
							}

							totalFsc = prevCdata == null ? 0 : prevCdata.getSc();
						}
						int totalShotCount = cdata.getSc() - totalFsc;

						if (totalShotCount > 1000) {
							return;
						}

						double ct = cdata.getCt();
						String totalLst = cdata.getLst();
						if (totalShotCount <= 0) {
							return;
						}

						List<Statistics> stats = new ArrayList<>();

						String lst = totalLst;
						int sc = cdata.getSc();
						int hourlySc = ValueUtils.toInteger(36000 / ct, 0);
						do {
							String fst;
							int shotCount;
							if (lst.endsWith("0000")) {
								if (totalShotCount >= hourlySc) {
									fst = DateUtils2.plus(lst, DatePattern.yyyyMMddHHmmss, Duration.ofHours(-1L));
									shotCount = hourlySc;
								} else {
									String minFst = DateUtils2.plus(lst, DatePattern.yyyyMMddHHmmss, Duration.ofHours(-1L));
									fst = DateUtils2.plus(lst, DatePattern.yyyyMMddHHmmss, Duration.ofSeconds(-toSeconds(totalShotCount, ct)));
									if (fst.compareTo(minFst) < 0) {
										throw new LogicException("UNEXPECTED_FST");
									}
									shotCount = totalShotCount;
								}
							} else {
								String minFst = DateUtils2.plus(lst, DatePattern.yyyyMMddHHmmss, Duration.ofSeconds(-toSeconds(totalShotCount, ct)));
								fst = lst.substring(0, 10) + "0000";
								if (fst.compareTo(minFst) <= 0) {
									fst = minFst;
									shotCount = totalShotCount;
								} else {
									long secs = DateUtils2.toInstant(lst, DatePattern.yyyyMMddHHmmss, Zone.GMT).getEpochSecond()
											- DateUtils2.toInstant(fst, DatePattern.yyyyMMddHHmmss, Zone.GMT).getEpochSecond();
									shotCount = ValueUtils.toInteger(secs / (ct / 10d), 0);
									if (shotCount > totalShotCount) {
										throw new LogicException("UNEXPECTED_SHOT_COUNT");
									}
								}
							}
							int fsc = ValueUtils.max(sc - shotCount, 0);
							long uptimeSeconds = ValueUtils.toLong(shotCount * (ct / 10d), 0L);

							Statistics stat = new Statistics();
							stat.setCdataId(cdata.getId());
							stat.setCi(cdata.getCi());
							stat.setTi(cdata.getTi());
							stat.setMoldId(cdata.getMoldId());
							stat.setMoldCode(cdata.getMoldCode());
							stat.setRt(cdata.getRt());
							stat.setCt(cdata.getCt());

							stat.setFst(fst);
							stat.setYear(fst.substring(0, 4));
							stat.setMonth(fst.substring(0, 6));
							stat.setDay(fst.substring(0, 8));
							stat.setHour(fst.substring(0, 10));
							stat.setWeek(DateUtils2.toOtherPattern(fst, DatePattern.yyyyMMddHHmmss, DatePattern.YYYYww));
							stat.setLst(lst);

							stat.setFsc(fsc);
							stat.setSc(sc);
							stat.setShotCount(shotCount);
							stat.setUptimeSeconds(uptimeSeconds);
							stat.setFirstData(fsc == 0);

							stat.setShotCountVal(stat.getShotCount());
							stat.setCtVal(stat.getCt());
//							stat.setLlct(0d);
//							stat.setUlct(0d);

							stats.add(0, stat);

							sc = fsc;
							lst = fst;
							totalShotCount -= shotCount;
						} while (totalShotCount > 0);

						BeanUtils.get(StatisticsRepository.class).saveAll(stats);
					});
				});
	}

	private static long toSeconds(int shotCount, double ct) {
		return ValueUtils.toLong(shotCount * (ct / 10d), 0L);
	}

	@Async
	@Transactional(propagation = Propagation.NEVER)
	public void adjustLogBatch(CdtIspAdjustIn input) {
		if (ObjectUtils.isEmpty(input.getCi())) {
			return;
		}
		JobUtils.runIfNotRunning("CdtIsp.adjust1stGenLogBatch", new JobOptions().setClustered(true), () -> {
			for (String ci : input.getCi()) {
				adjustLog(ci);
			}
		});
	}

	private void adjustLog(String ci) {
		QLogTransfer table = QLogTransfer.logTransfer;
		Instant inst = DateUtils2.toInstant("20230101", DatePattern.yyyyMMdd, Zone.GMT);

		DataUtils.runBatch(LogTransferRepository.class, //
				new BooleanBuilder()//
//						.and(table.at.eq("CDATA"))//
						.and(table.ci.eq(ci))//
						.and(table.ti.like("BTM%"))//
						.and(table.createdAt.goe(inst)), //
				Sort.by("id"), 1000, true, //
				logTransfer -> {
					if (!"CDATA".equals(logTransfer.getAt()) || ObjectUtils.isEmpty(logTransfer.getTi()) || !logTransfer.getTi().startsWith("BTM")) {
						return;
					}
					String es = logTransfer.getEs();
					String ds = logTransfer.getDs();
					Transfer transfer = TransferController.toTransfer(es, ds);
					BeanUtils.get(TransferService.class).procCdata(Arrays.asList(transfer));
				});
	}
}
