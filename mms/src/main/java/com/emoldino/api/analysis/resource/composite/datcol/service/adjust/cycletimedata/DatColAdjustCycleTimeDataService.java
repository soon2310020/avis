package com.emoldino.api.analysis.resource.composite.datcol.service.adjust.cycletimedata;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.api.analysis.resource.base.data.repository.data.Data;
import com.emoldino.api.analysis.resource.base.data.repository.data.DataRepository;
import com.emoldino.api.analysis.resource.base.data.repository.data.QData;
import com.emoldino.api.analysis.resource.base.data.repository.data3collected.Data3CollectedRepository;
import com.emoldino.api.analysis.resource.base.data.repository.data3collected.QData3Collected;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter.CycleTime;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounterRepository;
import com.emoldino.api.analysis.resource.base.data.repository.datacounter.QDataCounter;
import com.emoldino.api.analysis.resource.base.data.repository.statisticsext.StatisticsExt;
import com.emoldino.api.analysis.resource.base.data.repository.statisticsext.StatisticsExtRepository;
import com.emoldino.api.analysis.resource.base.data.repository.transferindex.QTransferIndex;
import com.emoldino.api.analysis.resource.base.data.repository.transferindex.TransferIndex;
import com.emoldino.api.analysis.resource.base.data.repository.transferindex.TransferIndexRepository;
import com.emoldino.api.analysis.resource.base.data.util.MoldDataUtils;
import com.emoldino.api.analysis.resource.composite.datcol.util.DatColUtils;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.CacheDataUtils;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import saleson.api.location.LocationRepository;
import saleson.api.statistics.StatisticsRepository;
import saleson.api.terminal.TerminalRepository;
import saleson.model.Cdata;
import saleson.model.Location;
import saleson.model.QStatistics;
import saleson.model.Statistics;
import saleson.model.Terminal;
import saleson.model.Transfer;
import saleson.service.data.service.DataMapper;
import saleson.service.transfer.CdataRepository;
import saleson.service.transfer.TransferRepository;
import saleson.service.transfer.TransferService;

@RequiredArgsConstructor
@Service
public class DatColAdjustCycleTimeDataService {

	private final JPAQueryFactory queryFactory;
	private final DataMapper dataMapper;

	public void adjustBatch() {
		JobUtils.runIfNotRunning("DatColAdjustCycleTime", new JobOptions().setClustered(true), () -> {
			findAndAdjustBatch();
		});
	}

	private void findAndAdjustBatch() {
		if (!TranUtils.doNewTran(() -> CacheDataUtils.exists("DatColAdjustCycleTimeDataService", "adjustDataCounter"))) {
			QData3Collected qData3 = QData3Collected.data3Collected;
			BooleanBuilder filter = new BooleanBuilder() //
					.and(qData3.dataType.eq("DATA")) //
					.and(qData3.dataId.isNotNull()) //
					.and(qData3.procStatus.in("REFINED"));

			DataUtils.runBatch(Data3CollectedRepository.class, filter, Sort.by("occurredAt"), 100, true, (data3) -> {
				TranUtils.doNewTran(() -> {
					Long dataId = data3.getDataId();

					// 1. Find
					QData qData = QData.data;
					Data data = queryFactory//
							.selectFrom(qData) //
							.where(qData.id.eq(dataId))//
							.fetchOne();//

					if (!ObjectUtils.isEmpty(data)) {
						Map<String, String> map = DatColUtils.separates(data.getRawData());

						if (!map.get(DataMapper.CDATA).isEmpty()) {
							DataCounter oldDc = dataMapper.toDataCounter(map.get(DataMapper.CDATA));
							DataCounter newDc = dataMapper.toDataCounter2(map.get(DataMapper.CDATA));

							if (oldDc == null || newDc == null || oldDc.getCycleTimes() == null || newDc.getCycleTimes() == null) {
								return;
							}

							if (oldDc.getCycleTimes().size() == newDc.getCycleTimes().size()) {
								return;
							}

							// 2. Adjust
							adjust(dataId, newDc);
						}
					}
				});
			});

			TranUtils.doNewTran(() -> CacheDataUtils.save("DatColAdjustCycleTimeDataService", "adjustDataCounter", null));
		}
	}

	public void findAndAdjustBatch(String counterId) {
		QData3Collected qData3 = QData3Collected.data3Collected;
		BooleanBuilder filter = new BooleanBuilder() //
				.and(qData3.deviceId.eq(counterId)) //)
				.and(qData3.dataType.eq("DATA")) //
				.and(qData3.dataId.isNotNull()) //
				.and(qData3.procStatus.in("REFINED"));

		DataUtils.runBatch(Data3CollectedRepository.class, filter, Sort.by("occurredAt"), 100, true, (data3) -> {
			TranUtils.doNewTran(() -> {
				Long dataId = data3.getDataId();

				// 1. Find
				QData qData = QData.data;
				Data data = queryFactory//
						.selectFrom(qData) //
						.where(qData.id.eq(dataId))//
						.fetchOne();//

				if (!ObjectUtils.isEmpty(data)) {
					Map<String, String> map = DatColUtils.separates(data.getRawData());

					if (!map.get(DataMapper.CDATA).isEmpty()) {
						DataCounter oldDc = dataMapper.toDataCounter(map.get(DataMapper.CDATA));
						DataCounter newDc = dataMapper.toDataCounter2(map.get(DataMapper.CDATA));

						if (oldDc == null || newDc == null || oldDc.getCycleTimes() == null || newDc.getCycleTimes() == null) {
							return;
						}

						if (oldDc.getCycleTimes().size() == newDc.getCycleTimes().size()) {
							return;
						}

						// 2. Adjust
						adjust(dataId, newDc);
					}
				}
			});
		});
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void adjustByDataId(Long dataId) {
		// 1. Find
		QData qData = QData.data;
		String rawData = queryFactory//
				.select(qData.rawData) //
				.from(qData) //
				.where(qData.id.eq(dataId))//
				.fetchOne();//
		DataCounter oldDc = dataMapper.toDataCounter(rawData);
		DataCounter newDc = dataMapper.toDataCounter2(rawData);
		if (oldDc.getCycleTimes().size() == newDc.getCycleTimes().size()) {
			return;
		}

		// 2. Adjust
		adjust(dataId, newDc);
	}

	private void adjust(Long dataId, DataCounter newDc) {
		TranUtils.doNewTran(() -> {

			QDataCounter qDataCounter = QDataCounter.dataCounter;
			// 1. Adjust - DataCounter
			DataCounter dataCounter = queryFactory //
					.selectFrom(qDataCounter) //
					.where(qDataCounter.dataId.eq(dataId)) //
					.fetchFirst();
			dataCounter.setCycleTimes(newDc.getCycleTimes());
			BeanUtils.get(DataCounterRepository.class).save(dataCounter);

			// 2. Adjust - Transfer

			// Get ZoneId 
			Terminal terminal = BeanUtils.get(TerminalRepository.class).findByEquipmentCode(dataCounter.getTerminalId()).orElse(null);
			String terminalId = dataCounter.getTerminalId();
			if (terminal != null) {
				Location location = BeanUtils.get(LocationRepository.class).findById(terminal.getLocationId()).orElse(null);
				if (location != null) {
					terminalId = dataCounter.getCreatedAt().compareTo(location.getUpdatedAt()) < 0 ? null : dataCounter.getTerminalId();
				}
			}
			String zoneId = LocationUtils.getZoneIdByTerminalCode(terminalId);

			Map<String, List<CycleTime>> shotsByShotEndTimeMap = new LinkedHashMap<>();
			Map<String, String> tempsByShotEndTimeMap = new LinkedHashMap<>();
			MoldDataUtils.populateByTime(dataCounter, shotsByShotEndTimeMap, tempsByShotEndTimeMap, zoneId);

			List<Transfer> transfers = queryFactory //
					.selectFrom(Q.transfer) //
					.where(Q.transfer.sn.eq(dataId.intValue()) //
							.and(Q.transfer.ci.startsWith("EMA"))) //
					.orderBy(Q.transfer.lst.asc()) //
					.fetch();

			AtomicInteger idxHolder = new AtomicInteger(0);
			shotsByShotEndTimeMap.forEach((shotEndTime, cycleTimes) -> {

				// 3. Adjust CycleTime - Cdata, Statistics		
				int idx = idxHolder.get();
				String tempStr = tempsByShotEndTimeMap.get(shotEndTime);

				// Case: Previously, data was one, but it was divided into two
				if (idx == 1 && transfers.size() == 1) {
					// Delete TransferIndex
					QTransferIndex table = QTransferIndex.transferIndex;
					BooleanBuilder filter = new BooleanBuilder();
					filter.and(table.ci.eq(dataCounter.getCounterId()));
					filter.and(table.tff.eq(shotEndTime));
					TransferIndex ti = BeanUtils.get(TransferIndexRepository.class).findOne(filter).orElse(null);
					if (ti != null) {
						BeanUtils.get(TransferIndexRepository.class).deleteById(ti.getId());
						BeanUtils.get(TransferIndexRepository.class).flush();
					}

					Transfer transfer = DatColUtils.toTransfer(dataCounter, shotEndTime, cycleTimes, tempStr, zoneId);
					BeanUtils.get(TransferService.class).procCdata(transfer);
					return;

				} else if (idx <= 1) {
					if (transfers.size() == 0) {
						Transfer transfer = DatColUtils.toTransfer(dataCounter, shotEndTime, cycleTimes, tempStr, zoneId);
						BeanUtils.get(TransferService.class).procCdata(transfer);
						return;
					}

					Transfer transfer = transfers.get(idx);

					// Get ReadTime
					Data data = BeanUtils.get(DataRepository.class).findById(dataId).orElse(null);
					String rt = null;
					if (data != null) {
						Instant recvTime = DateUtils2.toInstant(data.getReadTime(), DatePattern.yyyy_MM_dd_HH_mm_ss_SSS, Zone.GMT);
						rt = DateUtils2.format(recvTime, DatePattern.yyyyMMddHHmmss, zoneId);
						transfer.setRt(rt);
					} else {
						transfer.setRt(shotEndTime);
					}

					transfer.setTff(shotEndTime);
					DatColUtils.setCtValues(transfer, cycleTimes, shotEndTime, zoneId);
					DatColUtils.setTempValues(transfer, tempStr);

					if (!ObjectUtils.isEmpty(BeanUtils.get(TransferRepository.class).findById(transfer.getId()))) {
						BeanUtils.get(TransferRepository.class).save(transfer);
						BeanUtils.get(TransferRepository.class).flush();
					}

					Cdata cdata = BeanUtils.get(CdataRepository.class).findById(transfer.getId())
							.orElseThrow(() -> DataUtils.newDataNotFoundException(Cdata.class, "id", transfer.getId()));

					Statistics statistics = BeanUtils.get(StatisticsRepository.class).findFirstByCdataId(cdata.getId()) //
							.orElseThrow(() -> DataUtils.newDataNotFoundException(Statistics.class, "CdataId", cdata.getId()));

					populateCdataAndStatistics(transfer, cdata, statistics);

					BeanUtils.get(CdataRepository.class).save(cdata);
					BeanUtils.get(StatisticsRepository.class).save(statistics);
					BeanUtils.get(CdataRepository.class).flush();
					BeanUtils.get(StatisticsRepository.class).flush();

					// 4. Adjust CycleTime - StatisticsExt
					StatisticsExt statisticsExt = BeanUtils.get(StatisticsExtRepository.class).findByCdataId(cdata.getId()).orElse(null);
					if (statisticsExt != null) {
						adjustStatisticsExtData(cdata, statistics, statisticsExt);
						BeanUtils.get(StatisticsExtRepository.class).save(statisticsExt);
						BeanUtils.get(StatisticsExtRepository.class).flush();
					}

				}

				idxHolder.set(idxHolder.get() + 1);
			});
		});
	}

	private void populateCdataAndStatistics(Transfer transfer, Cdata cdata, Statistics statistics) {

		// Cdata
		{
			cdata.setSc(transfer.getSc());
			cdata.setLst(transfer.getLst());
			cdata.setCt(transfer.getCt());
			cdata.setRt(transfer.getRt());

			cdata.setCtt(transfer.getCtt());
			cdata.setLlct(transfer.getLlct());
			cdata.setUlct(transfer.getUlct());
			cdata.setTff(transfer.getTff());

			cdata.setYear(ValueUtils.abbreviate(transfer.getLst(), 4));
			cdata.setMonth(ValueUtils.abbreviate(transfer.getLst(), 6));
			cdata.setWeek(DateUtils2.toOtherPattern(transfer.getLst(), DatePattern.yyyyMMddHHmmss, DatePattern.YYYYww));
			cdata.setDay(ValueUtils.abbreviate(transfer.getLst(), 8));
		}

		// Statistics		
		{
			statistics.setSc(transfer.getSc());
			statistics.setLst(transfer.getLst());
			statistics.setRt(transfer.getRt());

			statistics.setCt(transfer.getCt());
			statistics.setLlct(transfer.getLlct());
			statistics.setUlct(transfer.getUlct());
			statistics.setTff(transfer.getTff());

			// Set Shot Count
			if (ConfigUtils.getServerName().equalsIgnoreCase("dyson") && statistics.getCt() <= 20 && statistics.getCt() > 0) {
				statistics.setShotCount(0);
			} else {
				statistics.setShotCount(DatColUtils.calcIncrShotCount(transfer.getCtt()));
			}

			// Query Previous LST data
			Statistics prevStat = null;
			{
				QStatistics table = QStatistics.statistics;
				BooleanBuilder filter = new BooleanBuilder();
				filter.and(table.lst.lt(statistics.getLst()));
				Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "lst", "id"));

				if (cdata.getMoldCode() != null) {
					filter.and(table.ci.eq(cdata.getCi())).and(table.moldId.eq(cdata.getMoldId()));
				} else {
					filter.and(table.ci.eq(cdata.getCi()));
				}
				Page<Statistics> page = BeanUtils.get(StatisticsRepository.class).findAll(filter, pageable);
				if (!page.getContent().isEmpty()) {
					prevStat = page.getContent().get(0);
				}
			}

			// Set fst
			if (prevStat == null) {
				statistics.setFsc(0);
				statistics.setFirstData(true);
			} else {
				statistics.setFsc(prevStat.getSc());
				statistics.setFirstData(false);
			}

			// Set Statistical time
			long uptimeSeconds = (long) (statistics.getShotCount() * (statistics.getCt() * 0.1));
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());

			String yyyy = DateUtils2.format(DateUtils2.getInstant().plus(Duration.ofDays(365 * 2)), DatePattern.yyyy, Zone.GMT);
			String statTime = ValueUtils.toString(statistics.getTff(), statistics.getRt());
			if (!ObjectUtils.isEmpty(statTime) && yyyy.compareTo(statTime) < 0 && !ObjectUtils.isEmpty(statistics.getRt()) && yyyy.compareTo(statistics.getRt()) >= 0) {
				statTime = statistics.getRt();
			}

			statistics.setYear(ValueUtils.abbreviate(statTime, 4));
			statistics.setMonth(ValueUtils.abbreviate(statTime, 6));
			statistics.setDay(ValueUtils.abbreviate(statTime, 8));
			statistics.setHour(ValueUtils.abbreviate(statTime, 10));
			statistics.setWeek(DateUtils2.toOtherPattern(statTime, DatePattern.yyyyMMddHHmmss, DatePattern.YYYYww));

			LocalDateTime startDate = LocalDateTime.parse(statistics.getHour() + "0000", formatter);
			LocalDateTime endDate = startDate.plus(Duration.ofSeconds(uptimeSeconds));
			if (uptimeSeconds > 3600) {
				endDate = startDate.plus(Duration.ofSeconds(3600));
			}

			String fst = startDate.format(formatter);
			String lst = endDate.format(formatter);

			statistics.setFst(fst);
			statistics.setLst(lst);
			statistics.setUptimeSeconds(uptimeSeconds);
		}

		// populateValidShot - Cdata, Statistics 		
		DatColUtils.populateValidShot(cdata, statistics, null, true);

	}

	private void adjustStatisticsExtData(Cdata cdata, Statistics statistics, StatisticsExt statisticsExt) {
		if (statistics.getShotCount() == null || cdata.getCtt() == null || ObjectUtils.isEmpty(StringUtils.replace(cdata.getCtt(), "/", ""))) {
			statisticsExt.setShotCountCtt(0);
			statisticsExt.setShotCountCttVal(0);
		} else {
			statisticsExt.setShotCountCtt(statistics.getShotCount());
			if (statistics.getCt() == null || statistics.getCt() * statistics.getShotCount() > 72000) {
				statisticsExt.setShotCountCttVal(0);
			} else {
				statisticsExt.setShotCountCttVal(statistics.getShotCount());
			}
		}

	}

}
