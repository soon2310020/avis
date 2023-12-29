package com.emoldino.api.analysis.resource.composite.datcol.service.correctleafyeardata;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.api.analysis.resource.base.data.repository.data3collected.Data3Collected;
import com.emoldino.api.analysis.resource.base.data.repository.data3collected.Data3CollectedRepository;
import com.emoldino.api.analysis.resource.base.data.repository.data3collected.QData3Collected;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.DataAcceleration;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.DataAccelerationRepository;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.QDataAcceleration;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounterRepository;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.QDataCounter;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.CacheDataUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.api.counter.CounterRepository;
import saleson.api.statistics.StatisticsRepository;
import saleson.model.Cdata;
import saleson.model.Counter;
import saleson.model.QCounter;
import saleson.model.QStatistics;
import saleson.model.QTransfer;
import saleson.model.Statistics;
import saleson.model.Transfer;
import saleson.service.transfer.CdataRepository;
import saleson.service.transfer.TransferRepository;

@Async
@Transactional(propagation = Propagation.NEVER)
@Service
public class DatColCorrectLeafYearDataService {

	public void post() {
//		StringBuilder buf = new StringBuilder();
//		post("EMA2252M10143", false, buf);
//		System.out.println(buf);

		JobUtils.runIfNotRunning("CorrectLeafYearData", new JobOptions().setClustered(true), () -> {
			boolean set = true;
			StringBuilder buf = null;

			// All Counters Loop
			int pageNo[] = { 0 };
			Page<Counter> page;
			while (!(page = TranUtils.doNewTran(() -> BeanUtils.get(CounterRepository.class).findAll(new BooleanBuilder().and(QCounter.counter.equipmentCode.like("EMA%")),
					PageRequest.of(pageNo[0]++, 100, Direction.ASC, "id")))).isEmpty()) {
				for (Counter counter : page.getContent()) {
					if (CacheDataUtils.exists("CorrectLeafYearData2", counter.getEquipmentCode())) {
						continue;
					}
					post(counter.getEquipmentCode(), set, buf);
					CacheDataUtils.save("CorrectLeafYearData2", counter.getEquipmentCode(), null);
				}
			}

//			if (buf != null) {
//				System.out.println(buf);
//			}
		});
	}

	private void post(String deviceId, boolean set, StringBuilder allBuf) {
		StringBuilder buf = allBuf == null ? null : new StringBuilder();
		String rn = "\r\n";
		String rnt = "\r\n\t";

		QData3Collected qData3 = QData3Collected.data3Collected;
		BooleanBuilder filter = new BooleanBuilder()//
				.and(qData3.dataType.eq("DATA"))//
				.and(qData3.deviceId.eq(deviceId))//
				.and(qData3.occurredAt.goe(DateUtils2.toInstant("20230228000000", DatePattern.yyyyMMddHHmmss, Zone.GMT)))//
				.and(qData3.occurredAt.lt(DateUtils2.toInstant("20230310000000", DatePattern.yyyyMMddHHmmss, Zone.GMT)));

		// Has been turned on
		boolean turnedOn[] = { false };
		// The first time after turnedOn
		boolean first[] = { false };
		// Has been turned off
		boolean turnedOff[] = { false };
		String lastShotEndedAt = null;
		String lastLst[] = { null };

		// Data3Collected Loop
		int pageNo[] = { 0 };
		Page<Data3Collected> page;
		while (!(page = //
				TranUtils.doNewTran(() -> BeanUtils.get(Data3CollectedRepository.class).findAll(filter, PageRequest.of(pageNo[0]++, 100, Direction.ASC, "occurredAt")))//
		).isEmpty()) {
			if (turnedOff[0]) {
				break;
			}

			for (Data3Collected data3 : page.getContent()) {
				Long dataId = data3.getDataId();
				if (dataId == null) {
					continue;
				}

				if (ObjectUtils.isEmpty(data3.getData())) {
					continue;
				}
				String[] strs = StringUtils.tokenizeToStringArray(data3.getData(), "/");
				if (strs.length < 3) {
					continue;
				}

				String type = strs[1];
				String shotStartedAt = strs[2];
				String shotEndedAt = strs.length > 3 ? strs[3] : strs[2];
				first[0] = false;
				if (!turnedOn[0]) {
					if (shotEndedAt.startsWith("20230229") || shotStartedAt.startsWith("20230229")) {
						turnedOn[0] = true;
						first[0] = shotStartedAt.startsWith("20230228");
					}
				} else if (!turnedOff[0] && lastShotEndedAt != null //
						&& DateUtils2.toInstant(shotEndedAt, DatePattern.yyyyMMddHHmmss, Zone.GMT).toEpochMilli()
								- DateUtils2.toInstant(lastShotEndedAt, DatePattern.yyyyMMddHHmmss, Zone.GMT).toEpochMilli() > (3600000 * 23)) {
					turnedOff[0] = true;
				}
				lastShotEndedAt = shotEndedAt;

				if (buf != null) {
					buf.append(rn).append(type)//
							.append("/").append(turnedOn[0]).append("/").append(turnedOff[0])//
							.append("/").append(shotStartedAt).append("/").append(shotEndedAt)//
							.append("/").append(data3.getDataId());
				}

				if (!turnedOn[0]) {
					continue;
				} else if (turnedOff[0]) {
					break;
				}

				TranUtils.doNewTran(() -> {
					String zoneId = LocationUtils.getZoneIdByCounterCode(deviceId);

					Instant maxInst = DateUtils2.toInstant(shotEndedAt, DatePattern.yyyyMMddHHmmss, Zone.GMT).plus(Duration.ofDays(1)).plus(Duration.ofSeconds(10));
					String maxGmt = DateUtils2.format(maxInst, DatePattern.yyyyMMddHHmmss, Zone.GMT);
					String max = DateUtils2.format(maxInst, DatePattern.yyyyMMddHHmmss, zoneId);

					List<DataCounter> counters = StreamSupport.stream(//
							BeanUtils.get(DataCounterRepository.class)//
									.findAll(new BooleanBuilder().and(QDataCounter.dataCounter.dataId.eq(dataId)), Sort.by("id"))//
									.spliterator(), //
							false//
					).collect(Collectors.toList());

					List<DataAcceleration> accels = StreamSupport.stream(//
							BeanUtils.get(DataAccelerationRepository.class)//
									.findAll(new BooleanBuilder().and(QDataAcceleration.dataAcceleration.dataId.eq(dataId)), Sort.by("id"))//
									.spliterator(), //
							false//
					).collect(Collectors.toList());

					List<DataCounter> cnts = new ArrayList<>();
					List<DataAcceleration> accs = new ArrayList<>();
					List<Transfer> trss = new ArrayList<>();
					List<Cdata> cdts = new ArrayList<>();
					List<Statistics> stas = new ArrayList<>();

					for (DataCounter cnt : counters) {
						boolean changed = false;
						StringBuilder buf2 = buf == null ? null : new StringBuilder();
						if (buf2 != null) {
							buf2.append(rnt).append("CNT");
						}
						String shotStartTime = correct(buf2, "sst", cnt.getShotStartTime(), first[0], shotStartedAt, maxGmt);
						if (shotStartTime != null && !shotStartTime.equals(cnt.getShotStartTime())) {
							if (set) {
								cnt.setShotStartTime(shotStartTime);
								if (!cnts.contains(cnt)) {
									cnts.add(cnt);
								}
							}
							changed = true;
						}
						String shotEndTime = correct(buf2, "set", cnt.getShotEndTime(), first[0], shotEndedAt, maxGmt);
						if (shotEndTime != null && !shotEndTime.equals(cnt.getShotEndTime())) {
							if (set) {
								cnt.setShotEndTime(shotEndTime);
								if (!cnts.contains(cnt)) {
									cnts.add(cnt);
								}
							}
							changed = true;
						}
						if (changed) {
							if (buf != null && buf2 != null) {
								buf.append(buf2);
							}
						}
					}

					for (DataAcceleration acc : accels) {
						boolean changed = false;
						StringBuilder buf2 = new StringBuilder();
						buf2.append(rnt).append("ACC");
						String measureTime = correct(buf2, "mst", acc.getMeasurementDate(), first[0], null, maxGmt);
						if (measureTime != null && !measureTime.equals(acc.getMeasurementDate())) {
							if (set) {
								acc.setMeasurementDate(measureTime);
								if (!accs.contains(acc)) {
									accs.add(acc);
								}
							}
							changed = true;
						}
						if (changed) {
							if (buf != null && buf2 != null) {
								buf.append(buf2);
							}
						}
					}

					if (!first[0]) {
						List<TransferSummary> summaries = new ArrayList<>();
						BeanUtils.get(TransferRepository.class)//
								.findAll(new BooleanBuilder()//
										.and(QTransfer.transfer.ci.eq(deviceId))//
										.and(QTransfer.transfer.sn.eq(dataId.intValue()))//
										, Sort.by("id")//
						).forEach(transfer -> {
							TransferSummary summary = new TransferSummary();
							summaries.add(summary);

							summary.setTransfer(transfer);
							Cdata cdata = BeanUtils.get(CdataRepository.class).findById(transfer.getId()).orElse(null);
							summary.setCdata(cdata);
							List<Statistics> stats = StreamSupport.stream(//
									BeanUtils.get(StatisticsRepository.class)//
											.findAll(new BooleanBuilder().and(QStatistics.statistics.cdataId.eq(transfer.getId())), Sort.by("id"))//
											.spliterator(), //
									false//
							).collect(Collectors.toList());
							summary.setStatistics(stats);
						});

						for (TransferSummary summary : summaries) {
							if (summary.getTransfer() == null) {
								continue;
							}

							Transfer trs = summary.getTransfer();
							Cdata cdata = summary.getCdata();
							boolean changed = false;
							StringBuilder buf2 = new StringBuilder();
							buf2.append(rnt).append("TRS");
							// TFF
							String oldTff = trs.getTff();
							String tff = correct(buf2, "tff", trs.getTff(), first[0], null, max);
							String maxLst = tff == null ? null : (ValueUtils.toLong(tff, 0L) + 100L) + "";
							if (tff != null && !tff.equals(trs.getTff())) {
								if (set) {
									trs.setTff(tff);
									if (!trss.contains(trs)) {
										trss.add(trs);
									}
									if (cdata != null) {
										cdata.setTff(tff);
										if (!cdts.contains(cdata)) {
											cdts.add(cdata);
										}
									}
									if (!ObjectUtils.isEmpty(summary.getStatistics())) {
										for (Statistics stat : summary.getStatistics()) {
											stat.setTff(tff);
											stat.setYear(ValueUtils.abbreviate(tff, 4));
											stat.setMonth(ValueUtils.abbreviate(tff, 6));
											stat.setDay(ValueUtils.abbreviate(tff, 8));
											stat.setHour(ValueUtils.abbreviate(tff, 10));
											stat.setWeek(DateUtils2.toOtherPattern(tff, DatePattern.yyyyMMddHHmmss, DatePattern.YYYYww));
											if (!stas.contains(stat)) {
												stas.add(stat);
											}
										}
									}
								}
								changed = true;
							}
							// LST
							String lst = trs.getLst();
							if (!ObjectUtils.isEmpty(lst) && oldTff != null && oldTff.equals(lst) && tff != null && !tff.equals(lst)) {
								lst = tff;
								if (set) {
									trs.setLst(lst);
									if (!trss.contains(trs)) {
										trss.add(trs);
									}
									if (cdata != null) {
										cdata.setLst(lst);
										cdata.setYear(ValueUtils.abbreviate(lst, 4));
										cdata.setMonth(ValueUtils.abbreviate(lst, 6));
										cdata.setDay(ValueUtils.abbreviate(lst, 8));
										cdata.setWeek(DateUtils2.toOtherPattern(lst, DatePattern.yyyyMMddHHmmss, DatePattern.YYYYww));
										if (!cdts.contains(cdata)) {
											cdts.add(cdata);
										}
									}
								}
								changed = true;
							}
							lastLst[0] = lst;
							if (!ObjectUtils.isEmpty(summary.getStatistics())) {
								for (Statistics stat : summary.getStatistics()) {
									String sfst = correct(buf2, "fst", stat.getFst(), first[0], null, tff);
									if (sfst != null && !sfst.equals(stat.getFst())) {
										if (set) {
											stat.setFst(sfst);
											if (!stas.contains(stat)) {
												stas.add(stat);
											}
										}
										changed = true;
									}
									String slst = correct(buf2, "lst", stat.getLst(), first[0], null, maxLst);
									if (slst != null && !slst.equals(stat.getLst())) {
										if (set) {
											stat.setLst(slst);
											if (!stas.contains(stat)) {
												stas.add(stat);
											}
										}
										changed = true;
									}
								}
							}

//							if (changed) {
							if (buf != null && buf2 != null) {
								buf.append(buf2);
							}
//							}
						}
					}

					BeanUtils.get(DataCounterRepository.class).saveAll(cnts);
					BeanUtils.get(DataAccelerationRepository.class).saveAll(accs);
					BeanUtils.get(TransferRepository.class).saveAll(trss);
					BeanUtils.get(CdataRepository.class).saveAll(cdts);
					BeanUtils.get(StatisticsRepository.class).saveAll(stas);
				});
			}
		}

		if (allBuf != null && buf != null && turnedOn[0]) {
			allBuf.append(rn).append(buf);
		}
	}

	private static String correct(StringBuilder buf, String field, String from, boolean first, String origin, String max) {
		if (ObjectUtils.isEmpty(from) //
				|| (first && from.length() > 10 && from.substring(4, 10).equals("022823"))//
				|| (!ObjectUtils.isEmpty(origin) && !from.substring(0, 8).equals(origin.substring(0, 8)))//
		) {
			if (buf != null) {
				buf.append("/").append(field).append(":").append(from).append("_X");
			}
			return from;
		}
		String to = DateUtils2.format(//
				DateUtils2.toInstant(from, DatePattern.yyyyMMddHHmmss, Zone.GMT).plus(Duration.ofDays(1L)), //
				DatePattern.yyyyMMddHHmmss, Zone.GMT//
		);
		if (!ObjectUtils.isEmpty(max) && to.compareTo(max) > 0) {
			if (buf != null) {
				buf.append("/").append(field).append(":").append(from).append("_X");
			}
			return from;
		}
		if (buf != null) {
			buf.append("/").append(field).append(":").append(from).append("_").append(to);
		}
		return to;
	}

	@lombok.Data
	public static class TransferSummary {
		private Transfer transfer;
		private Cdata cdata;
		private List<Statistics> statistics;
	}

}
