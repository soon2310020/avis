package com.emoldino.api.analysis.resource.base.data.service.expired;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.emoldino.api.analysis.resource.base.data.repository.data.Data;
import com.emoldino.api.analysis.resource.base.data.repository.data.DataRepository;
import com.emoldino.api.analysis.resource.base.data.repository.data3collected.Data3CollectedRepository;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.DataAcceleration;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.DataAccelerationRepository;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounterRepository;
import com.emoldino.api.analysis.resource.base.data.repository.statisticsext.StatisticsExt;
import com.emoldino.api.analysis.resource.base.data.repository.statisticsext.StatisticsExtRepository;
import com.emoldino.api.analysis.resource.base.data.repository.transferindex.TransferIndex;
import com.emoldino.api.analysis.resource.base.data.repository.transferindex.TransferIndexRepository;
import com.emoldino.api.analysis.resource.base.data.repository.transferresult.TransferResult;
import com.emoldino.api.analysis.resource.base.data.repository.transferresult.TransferResultRepository;
import com.emoldino.api.common.resource.base.client.util.ClientUtils;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.repository.Qs;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.Closure1Param;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.api.statistics.StatisticsPartRepository;
import saleson.api.statistics.StatisticsRepository;
import saleson.model.Cdata;
import saleson.model.LogTransfer;
import saleson.model.Statistics;
import saleson.model.StatisticsPart;
import saleson.model.Tdata;
import saleson.model.Transfer;
import saleson.service.transfer.CdataRepository;
import saleson.service.transfer.LogTransferRepository;
import saleson.service.transfer.TdataRepository;
import saleson.service.transfer.TransferRepository;

@Service
public class ExpiredDataService {

	@Async
	public void cleanBatch() {
		String clientCode = ConfigUtils.getServerName();
		if (!ClientUtils.MABE.equals(clientCode)) {
			return;
		}

//		_cleanBatch();
	}

	private void _cleanBatch() {
		JobUtils.runIfNotRunning("ExpiredDataService.cleanBatch", new JobOptions().setClustered(true), () -> {
			Instant instant = DateUtils2.getInstant();
			int thisYear = ValueUtils.toInteger(DateUtils2.format(instant, "yyyy", Zone.GMT), 0);
			int thisMonth = ValueUtils.toInteger(DateUtils2.format(instant, "MM", Zone.GMT), 0);
			if (thisMonth >= 10) {
				instant = DateUtils2.toInstant(thisYear + "0401", DatePattern.yyyyMMdd, Zone.GMT);
			} else if (thisMonth >= 7) {
				instant = DateUtils2.toInstant(thisYear + "0101", DatePattern.yyyyMMdd, Zone.GMT);
			} else if (thisMonth >= 4) {
				instant = DateUtils2.toInstant((thisYear - 1) + "1001", DatePattern.yyyyMMdd, Zone.GMT);
			} else {
				instant = DateUtils2.toInstant((thisYear - 1) + "0701", DatePattern.yyyyMMdd, Zone.GMT);
			}
			Instant before = instant.minus(Duration.ofDays(1L));
			Instant before1w = before.minus(Duration.ofDays(7L));
			Instant before3m = DateUtils2.plusMonths(before, -3, Zone.GMT);

//			ACCESS_SUMMARY_LOG
//			BATCH_MESSAGE
//			BROADCAST_NOTIFICATION
//			BROADCAST_NOTIFICATION_230118
//			ERROR_SUMMARY_LOG
//			LOG_AUTHENTICATION
//			LOG_DISCONNECTION
//			LOG_USER_ALERT
//			MOLD_EFFICIENCY

//			DataUtils.runContentBatch(AccessSummaryLogRepository.class, //
//					Qs.accessSummaryLog.updatedAt.before(before), //
//					Sort.by("id"), 100, false, //
//					content -> TranUtils.doNewTran(() -> {
//						BeanUtils.get(AccessSummaryLogRepository.class).deleteAll(content);
//					}));
//
//			DataUtils.runContentBatch(ErrorSummaryLogRepository.class, //
//					Qs.errorSummaryLog.updatedAt.before(before), //
//					Sort.by("id"), 100, false, //
//					content -> TranUtils.doNewTran(() -> {
//						BeanUtils.get(ErrorSummaryLogRepository.class).deleteAll(content);
//					}));

//			DATA3COLLECTED
//			DATA
//			DATA_COUNTER
//			DATA_ACCELERATION
//			TRANSFER
//			TDATA
//			CDATA
//			STATISTICS
//			STATISTICS_EXT
//			STATISTICS_PART
//			(PRESET)
//			LOG_TRANSFER
//			TRANSFER_RESULT

			DataUtils.runBatchWhenHasNext(BeanUtils.get(JPAQueryFactory.class)//
					.selectFrom(Qs.data3Collected)//
					.where(Qs.data3Collected.dataType.in("HEARTBEAT", "DATA"))//
					.orderBy(Qs.data3Collected.id.asc()), // 
					100, false, //
					data3 -> TranUtils.doNewTran(() -> {
						if (before.compareTo(data3.getUpdatedAt()) <= 0) {
							return false;
						}

						if ("HEARTBEAT".equals(data3.getDataType())) {
							BeanUtils.get(Data3CollectedRepository.class).delete(data3);
							return true;
//						} else if (!"DATA".equals(data3.getDataType())) {
//							return true;
						} else if (data3.getDataId() == null) {
							BeanUtils.get(Data3CollectedRepository.class).delete(data3);
							return true;
						}

						Long dataId = data3.getDataId();
						Integer dataIdInt = ValueUtils.toInteger(dataId, 0);
						String ci = data3.getDeviceId();

						Data data = BeanUtils.get(DataRepository.class).findById(dataId).orElse(null);

//						List<DataCounter> dataCnts = new ArrayList<>();
//						BeanUtils.get(DataCounterRepository.class)//
//								.findAll(Qs.dataCounter.dataId.eq(dataId).and(Qs.dataCounter.counterId.eq(ci)), Sort.by("id"))//
//								.forEach(dataCnts::add);
//
//						List<DataAcceleration> dataAccs = new ArrayList<>();
//						BeanUtils.get(DataAccelerationRepository.class)//
//								.findAll(Qs.dataAcceleration.dataId.eq(dataId).and(Qs.dataAcceleration.counterId.eq(ci)), Sort.by("id"))//
//								.forEach(dataAccs::add);

						List<Transfer> trss = new ArrayList<>();
						List<Long> cdataIds = new ArrayList<>();
						BeanUtils.get(TransferRepository.class)//
								.findAll(Q.transfer.at.eq("CDATA").and(Q.transfer.ci.eq(ci)).and(Q.transfer.sn.eq(dataIdInt)), Sort.by("id"))//
								.forEach(item -> {
									trss.add(item);
									cdataIds.add(item.getId());
								});

						List<Cdata> cdatas = new ArrayList<>();
						BeanUtils.get(CdataRepository.class)//
								.findAll(Q.cdata.id.in(cdataIds), Sort.by("id"))//
								.forEach(cdatas::add);

						List<Statistics> stats = new ArrayList<>();
						List<Long> statIds = new ArrayList<>();
						BeanUtils.get(StatisticsRepository.class)//
								.findAll(Q.statistics.cdataId.in(cdataIds), Sort.by("id"))//
								.forEach(item -> {
									stats.add(item);
									statIds.add(item.getId());
								});

						List<StatisticsExt> statExts = new ArrayList<>();
						BeanUtils.get(StatisticsExtRepository.class)//
								.findAll(Q.statisticsExt.id.in(statIds), Sort.by("id"))//
								.forEach(statExts::add);

						List<StatisticsPart> statParts = new ArrayList<>();
						BeanUtils.get(StatisticsPartRepository.class)//
								.findAll(Q.statisticsPart.statisticsId.in(statIds), Sort.by("id"))//
								.forEach(statParts::add);

						if (!statParts.isEmpty()) {
							BeanUtils.get(StatisticsPartRepository.class).deleteAll(statParts);
						}
						if (!statExts.isEmpty()) {
							BeanUtils.get(StatisticsExtRepository.class).deleteAll(statExts);
						}
						if (!stats.isEmpty()) {
							BeanUtils.get(StatisticsRepository.class).deleteAll(stats);
						}
						if (!cdatas.isEmpty()) {
							BeanUtils.get(CdataRepository.class).deleteAll(cdatas);
						}
						if (!trss.isEmpty()) {
							BeanUtils.get(TransferRepository.class).deleteAll(trss);
						}
//						if (!dataAccs.isEmpty()) {
//							BeanUtils.get(DataAccelerationRepository.class).deleteAll(dataAccs);
//						}
//						if (!dataCnts.isEmpty()) {
//							BeanUtils.get(DataCounterRepository.class).deleteAll(dataCnts);
//						}
						if (data != null) {
							BeanUtils.get(DataRepository.class).delete(data);
						}
						if (data3 != null) {
							BeanUtils.get(Data3CollectedRepository.class).delete(data3);
						}

						return true;
					}));

			DataUtils.runContentBatchWhenHasNext(BeanUtils.get(JPAQueryFactory.class)//
					.selectFrom(Qs.data)//
					.orderBy(Qs.data.id.asc()), // 
					100, false, //
					content -> TranUtils.doNewTran(() -> {
						List<Data> list = extract(content, before1w, Data::getUpdatedDate);
						if (!list.isEmpty()) {
							BeanUtils.get(DataRepository.class).deleteAll(list);
						}
						return !list.isEmpty() && list.size() == content.size();
					}));

			Data data = QueryUtils.applyPagination(BeanUtils.get(JPAQueryFactory.class)//
					.selectFrom(Qs.data)//
					.orderBy(Qs.data.id.asc())//
					.limit(thisMonth), //
					PageRequest.of(0, 1, Sort.by("id"))).fetchFirst();

			if (data != null) {
				DataUtils.runContentBatchWhenHasNext(BeanUtils.get(JPAQueryFactory.class)//
						.selectFrom(Qs.dataCounter)//
						.orderBy(Qs.dataCounter.id.asc()), // 
						100, false, //
						content -> TranUtils.doNewTran(() -> {
							List<DataCounter> list = extract(content, item -> item.getDataId() < data.getId());
							if (!list.isEmpty()) {
								BeanUtils.get(DataCounterRepository.class).deleteAll(list);
							}
							return !list.isEmpty() && list.size() == content.size();
						}));

				DataUtils.runContentBatchWhenHasNext(BeanUtils.get(JPAQueryFactory.class)//
						.selectFrom(Qs.dataAcceleration)//
						.orderBy(Qs.dataAcceleration.id.asc()), // 
						100, false, //
						content -> TranUtils.doNewTran(() -> {
							List<DataAcceleration> list = extract(content, item -> item.getDataId() < data.getId());
							if (!list.isEmpty()) {
								BeanUtils.get(DataAccelerationRepository.class).deleteAll(list);
							}
							return !list.isEmpty() && list.size() == content.size();
						}));
			}

			DataUtils.runBatchWhenHasNext(BeanUtils.get(JPAQueryFactory.class)//
					.selectFrom(Q.transfer)//
					.orderBy(Q.transfer.id.asc()), // 
					100, false, //
					transfer -> TranUtils.doNewTran(() -> {
						if (before1w.compareTo(transfer.getCreatedAt()) <= 0) {
							return false;
						}

						if ("TDATA".equals(transfer.getAt())) {

							Tdata tdata = BeanUtils.get(TdataRepository.class)//
									.findById(transfer.getId()).orElse(null);
							if (tdata != null) {
								BeanUtils.get(TdataRepository.class).delete(tdata);
							}

						} else if ("CDATA".equals(transfer.getAt())) {
							Long cdataId = transfer.getId();

							Cdata cdata = BeanUtils.get(CdataRepository.class)//
									.findById(cdataId).orElse(null);

							List<Statistics> stats = new ArrayList<>();
							List<Long> statIds = new ArrayList<>();
							BeanUtils.get(StatisticsRepository.class)//
									.findAll(Q.statistics.cdataId.eq(cdataId), Sort.by("id"))//
									.forEach(item -> {
										stats.add(item);
										statIds.add(item.getId());
									});

							List<StatisticsExt> statExts = new ArrayList<>();
							BeanUtils.get(StatisticsExtRepository.class)//
									.findAll(Q.statisticsExt.id.in(statIds), Sort.by("id"))//
									.forEach(statExts::add);

							List<StatisticsPart> statParts = new ArrayList<>();
							BeanUtils.get(StatisticsPartRepository.class)//
									.findAll(Q.statisticsPart.statisticsId.in(statIds), Sort.by("id"))//
									.forEach(statParts::add);

							if (!statParts.isEmpty()) {
								BeanUtils.get(StatisticsPartRepository.class).deleteAll(statParts);
							}
							if (!statExts.isEmpty()) {
								BeanUtils.get(StatisticsExtRepository.class).deleteAll(statExts);
							}
							if (!stats.isEmpty()) {
								BeanUtils.get(StatisticsRepository.class).deleteAll(stats);
							}
							if (cdata != null) {
								BeanUtils.get(CdataRepository.class).delete(cdata);
							}

						}

						if (transfer != null) {
							BeanUtils.get(TransferRepository.class).delete(transfer);
						}

						return true;
					}));

			DataUtils.runContentBatchWhenHasNext(BeanUtils.get(JPAQueryFactory.class)//
					.selectFrom(Qs.transferIndex)//
					.orderBy(Qs.transferIndex.id.asc()), //
					100, false, //
					content -> TranUtils.doNewTran(() -> {
						List<TransferIndex> list = extract(content, before3m, TransferIndex::getCreatedAt);
						if (!list.isEmpty()) {
							BeanUtils.get(TransferIndexRepository.class).deleteAll(list);
						}
						return !list.isEmpty() && list.size() == content.size();
					}));

			DataUtils.runContentBatchWhenHasNext(BeanUtils.get(JPAQueryFactory.class)//
					.selectFrom(Qs.logTransfer)//
					.orderBy(Qs.logTransfer.id.asc()), //
					100, false, //
					content -> TranUtils.doNewTran(() -> {
						List<LogTransfer> list = extract(content, before1w, LogTransfer::getCreatedAt);
						if (list.isEmpty()) {
							return false;
						}
						List<Long> ids = new ArrayList<>();
						list.forEach(item -> ids.add(item.getId()));
						List<TransferResult> results = new ArrayList<>();
						BeanUtils.get(TransferResultRepository.class).findAll(Qs.transferResult.id.in(ids)).forEach(results::add);
						if (!results.isEmpty()) {
							BeanUtils.get(TransferResultRepository.class).deleteAll(results);
						}
						BeanUtils.get(LogTransferRepository.class).deleteAll(list);
						return !list.isEmpty() && list.size() == content.size();
					}));

//			STATISTICS_WUT
//			MOLD_CHART_STAT
//			PART_STAT
//			PRODUCED_PART
//			REJECTED_PART_DETAILS

//			DOWNTIME_ITEM

//			MACHINE_STATISTICS
//			MACHINE_MOLD_MATCHING_HISTORY
//			MACHINE_DOWNTIME_ALERT
//			MACHINE_DOWNTIME_REASON
//			MACHINE_OEE

		});

	}

	private <T> List<T> extract(List<T> content, Instant before, Closure1Param<T, Instant> closure) {
		List<T> list = new ArrayList<>();
		for (T item : content) {
			if (before.compareTo(closure.execute(item)) <= 0) {
				return list;
			}
			list.add(item);
		}
		return list;
	}

	private <T> List<T> extract(List<T> content, Closure1Param<T, Boolean> closure) {
		List<T> list = new ArrayList<>();
		for (T item : content) {
			if (!closure.execute(item)) {
				return list;
			}
			list.add(item);
		}
		return list;
	}

}
