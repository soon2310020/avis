package com.emoldino.api.analysis.resource.composite.cyctimdev.service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.emoldino.api.analysis.resource.base.data.repository.cycletimedeviation.CycleTimeDeviation;
import com.emoldino.api.analysis.resource.base.data.repository.cycletimedeviation.CycleTimeDeviationRepository;
import com.emoldino.api.analysis.resource.base.data.repository.cycletimedeviation.QCycleTimeDeviation;
import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevDetailsGetIn;
import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevDetailsGetOut;
import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevDetailsGetOut.CycTimDevDetails;
import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevGetIn;
import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevGetOut;
import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevItem;
import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevPart;
import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevPostIn;
import com.emoldino.api.analysis.resource.composite.cyctimdev.repository.CycTimDevRepository;
import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.common.resource.composite.flt.dto.FltPart;
import com.emoldino.api.common.resource.composite.flt.dto.FltPartIn;
import com.emoldino.api.common.resource.composite.flt.dto.FltSupplier;
import com.emoldino.api.common.resource.composite.flt.service.FltService;
import com.emoldino.api.common.resource.composite.flt.util.FltUtils;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import lombok.extern.slf4j.Slf4j;
import saleson.api.mold.MoldPartRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.report.payload.ProductivitySearchPayload;
import saleson.api.statistics.StatisticsRepository;
import saleson.common.enumeration.OutsideUnit;
import saleson.common.enumeration.productivity.CompareType;
import saleson.common.util.StringUtils;
import saleson.model.Mold;
import saleson.model.MoldPart;
import saleson.model.QStatistics;
import saleson.model.Statistics;
import saleson.model.data.cycleTime.ToolingCycleTimeData;

@Slf4j
@Service
public class CycTimDevService {

	private static final List<TimeScale> TIME_SCALE_SUPPORTED = Arrays.asList(TimeScale.DATE, TimeScale.WEEK, TimeScale.MONTH, TimeScale.YEAR, TimeScale.CUSTOM);

	@Autowired
	CycTimDevRepository repo;

	public CycTimDevGetOut getPage(CycTimDevGetIn input, Pageable pageable) {
		ValueUtils.assertTimeSetting(input, TIME_SCALE_SUPPORTED);
		Page<CycTimDevItem> page = repo.findAll(input, pageable);
		return new CycTimDevGetOut(page.getContent(), pageable, page.getTotalElements());
	}

	public CycTimDevDetailsGetOut getDetailsPage(CycTimDevDetailsGetIn input, Pageable pageable) {
		ValueUtils.assertTimeSetting(input, TIME_SCALE_SUPPORTED);
		FltSupplier supplier = FltUtils.getSupplierById(input.getSupplierId());
		Page<CycTimDevDetails> page = repo.findDetailsAll(input, pageable);
		//add part detail
		loadPart(page.getContent());
		return new CycTimDevDetailsGetOut(page.getContent(), pageable, page.getTotalElements(), supplier, null);
	}

	public void loadPart(List<CycTimDevDetails> list) {
		QueryUtils.includeDisabled(Q.mold);
		List<Long> moldIds = list.stream().map(cycTimDevDetails -> cycTimDevDetails.getMoldId()).collect(Collectors.toList());
		List<MoldPart> moldParts = BeanUtils.get(MoldPartRepository.class).findAllByMoldIdIn(moldIds);

		list.stream().forEach(cycTimDevDetail -> {
			FltPartIn partIn = new FltPartIn();
			partIn.setMoldId(Arrays.asList(cycTimDevDetail.getMoldId()));
			List<FltPart> parts = BeanUtils.get(FltService.class).getParts(partIn, PageRequest.of(0, 999)).getContent();
			List<CycTimDevPart> cycTimDevParts = parts.stream().map(part -> {
				Integer cavity = moldParts.stream().filter(mp -> mp.getMoldId().equals(cycTimDevDetail.getMoldId()) && mp.getPartId().equals(part.getId()))
						.map(mp -> mp.getCavity()).findFirst().orElse(0);
				return new CycTimDevPart(part.getId(), part.getName(), part.getPartCode(), cavity);
			}).collect(Collectors.toList());

			cycTimDevDetail.setParts(cycTimDevParts);
		});
	}

	public void post(CycTimDevPostIn input) {
		JobUtils.runIfNotRunning("CycTimDevService.post", new JobOptions().setClustered(true), () -> {
			QCycleTimeDeviation table = QCycleTimeDeviation.cycleTimeDeviation;
			Page<CycleTimeDeviation> page = TranUtils.doNewTran(() -> BeanUtils.get(CycleTimeDeviationRepository.class).findAll(//
					new BooleanBuilder().and(table.timeScale.eq(TimeScale.DATE)), //
					PageRequest.of(0, 1, Direction.DESC, "day")//
			));

//			Instant startDate = StringUtils.isEmpty(input.getStartDate()) ? //
//					DateUtils2.getInstant() : //
//					DateUtils2.toInstant(input.getStartDate(), DateUtils2.DatePattern.yyyyMMdd, DateUtils2.Zone.SYS);
//			Instant endDate = StringUtils.isEmpty(input.getEndDate()) ? //
//					DateUtils2.getInstant() : //
//					DateUtils2.toInstant(input.getEndDate(), DateUtils2.DatePattern.yyyyMMdd, DateUtils2.Zone.SYS);
			Instant fromInst;
			if (page.isEmpty()) {
				fromInst = DateUtils2.getInstant().minus(Duration.ofDays(365 * 5));
				String fromDay = DateUtils2.format(fromInst, DatePattern.yyyyMMdd, Zone.SYS);
				Page<Statistics> stats = TranUtils.doNewTran(() -> BeanUtils.get(StatisticsRepository.class).findAll(//
						new BooleanBuilder().and(QStatistics.statistics.day.goe(fromDay)), //
						PageRequest.of(0, 1, Direction.ASC, "day")//
				));
				if (stats.isEmpty()) {
					return;
				}
				fromInst = DateUtils2.toInstant(stats.getContent().get(0).getDay(), DatePattern.yyyyMMdd, Zone.SYS);
			} else {
				fromInst = DateUtils2.toInstant(page.getContent().get(0).getDay(), DatePattern.yyyyMMdd, Zone.SYS);
				String fromDay = DateUtils2.format(fromInst, DatePattern.yyyyMMdd, Zone.SYS);
				int i = 0;
				Page<CycleTimeDeviation> dayPage;
				while (i++ < 1000 && !(dayPage = TranUtils.doNewTran(() -> BeanUtils.get(CycleTimeDeviationRepository.class).findAll(//
						new BooleanBuilder()//
								.and(table.timeScale.eq(TimeScale.DATE))//
								.and(table.day.eq(fromDay)), //
						PageRequest.of(0, 1000, Direction.ASC, "day")//
				))).isEmpty()) {
					Page<CycleTimeDeviation> _dayPage = dayPage;
					TranUtils.doNewTran(() -> BeanUtils.get(CycleTimeDeviationRepository.class).deleteAll(_dayPage.getContent()));
				}
			}
			Instant toInst = DateUtils2.getInstant().minus(Duration.ofDays(2));
			//ignore empty data dates
			Page<Statistics> statsEnd = TranUtils.doNewTran(() -> BeanUtils.get(StatisticsRepository.class).findAll(//
					new BooleanBuilder().and(Q.statistics.moldId.isNotNull()), //
					PageRequest.of(0, 1, Sort.Direction.DESC, "day")//
			));
			if (!statsEnd.isEmpty() && !StringUtils.isEmpty(statsEnd.getContent().get(0).getDay())) {
				String day = statsEnd.getContent().get(0).getDay();
				Instant endDate = StringUtils.isEmpty(day) ? //
						DateUtils2.getInstant() : //
						DateUtils2.toInstant(day, DateUtils2.DatePattern.yyyyMMdd, DateUtils2.Zone.SYS);
				if (endDate.isBefore(toInst)) {
					toInst = endDate;
				}
			}

			Instant date = fromInst;
			do {
				log.info("Start calculateMoldCtDeviationByDay {}", date);
				postByDay(date);
				log.info("end calculateMoldCtDeviationByDay {}", date);
				date = date.plus(1, ChronoUnit.DAYS);
			} while (date.isBefore(toInst));
		});
	}

	public void postByDay(Instant date) {
		JobUtils.runIfNotRunning("CycTimDevService.postByDay", new JobOptions().setClustered(true), () -> {
			ProductivitySearchPayload payload = ProductivitySearchPayload.builder()//
					.compareBy(CompareType.TOOL)//
					.build();

			QCycleTimeDeviation table = QCycleTimeDeviation.cycleTimeDeviation;

			String day = DateUtils2.format(date, DateUtils2.DatePattern.yyyyMMdd, DateUtils2.Zone.SYS);
			String prevDay = DateUtils2.format(date.minus(1, ChronoUnit.DAYS), DateUtils2.DatePattern.yyyyMMdd, DateUtils2.Zone.SYS);
			String week = DateUtils2.format(date, DateUtils2.DatePattern.YYYYww, DateUtils2.Zone.SYS);
			String month = day.substring(0, 6);
			String year = month.substring(0, 4);

			payload.setStartDate(day);
			payload.setEndDate(day);

			int pageNo = 0;
			List<ToolingCycleTimeData> list;
			while (pageNo < 1000
					&& !(list = BeanUtils.get(MoldRepository.class).findToolingCycleTimeData(null, payload, PageRequest.of(pageNo++, 100, Sort.by("id")), true)).isEmpty()) {
				List<ToolingCycleTimeData> _list = list;
				TranUtils.doNewTran(() -> {
					_list.stream().forEach(ctData -> {
						Mold mold = BeanUtils.get(MoldRepository.class).findById(ctData.getMoldId()).orElse(null);
						if (mold == null) {
							return;
						}

						CycleTimeDeviation ctd = new CycleTimeDeviation();
						{
							BooleanBuilder filter = new BooleanBuilder()//
									.and(table.moldId.eq(mold.getId()))//
									.and(table.timeScale.eq(TimeScale.DATE))//
									.and(table.day.eq(day));
							Page<CycleTimeDeviation> page = BeanUtils.get(CycleTimeDeviationRepository.class).findAll(filter, PageRequest.of(0, 1, Sort.Direction.DESC, "id"));
							if (!page.isEmpty()) {
								ctd = page.getContent().get(0);
							} else {
								ctd.setMoldId(mold.getId());
								ctd.setSupplierId(mold.getLocation().getCompanyId());
								ctd.setTimeScale(TimeScale.DATE);
								ctd.setYear(year);
								ctd.setMonth(month);
								ctd.setWeek(week);
								ctd.setDay(day);
							}
						}
						ctd.bind(ctData);

						double approvedCt = ValueUtils.toDouble(mold.getContractedCycleTimeToCalculation(), 0d);
						ctd.setApprovedCycleTime(approvedCt);
						double averageCt = MoldUtils.getAverageCycleTime(mold.getId(), approvedCt, day, day, 1).getValue();
						ctd.setAverageCycleTime(averageCt);
						if (approvedCt != 0) {
							ctd.setNctd((averageCt - approvedCt) / approvedCt * 100);
						}

						//calculate trend
//						if (ctd.getNctd() != null) {
						Double nctdCurrent = ctd.getNctd() != null ? ctd.getNctd() : 0d;
						Page<CycleTimeDeviation> page = BeanUtils.get(CycleTimeDeviationRepository.class).findAll(//
								new BooleanBuilder()//
										.and(table.moldId.eq(mold.getId()))//
										.and(table.timeScale.eq(TimeScale.DATE))//
										.and(table.day.loe(prevDay)), //
								PageRequest.of(0, 1, Sort.Direction.DESC, "day"));

						if (!page.isEmpty() && page.getContent().get(0).getNctd() != null) {
							Double prevNctd = page.getContent().get(0).getNctd();
							if (prevNctd != null && prevNctd != 0) {
								Double trend = (nctdCurrent - prevNctd) / prevNctd * 100;
								ctd.setNctdTrend(trend);
							}
						}
//						}
						//Cycle time fluctuation
						calcCycleTimeFluctuation(ctd, mold, day, prevDay, approvedCt, averageCt);

						BeanUtils.get(CycleTimeDeviationRepository.class).save(ctd);
					});
				});

				log.info("MoldCtDeviation at {} size + {}", day, list.size());
			}
		});
	}

	protected static CycTimDevGetOut dummyGetPage(CycTimDevGetIn input, Pageable pageable) {
		List<CycTimDevItem> content = new ArrayList<>();
		{
			CycTimDevItem item = CycTimDevItem.builder()//
					.supplierId(1L)//
					.supplierName("Supplier A")//
					.supplierCode("SA")//
					.moldCount(3L)//
					.aboveTolerance(24.1)//
					.withinUpperL2Tolerance(19.9)//
					.withinL1Tolerance(13.3)//
					.withinLowerL2Tolerance(40.4)//
					.belowTolerance(2.3)//
					.nctd(0.21)//
					.nctdTrend(6.4)//
					.build();
			content.add(item);
		}
		{
			CycTimDevItem item = CycTimDevItem.builder()//
					.supplierId(2L)//
					.supplierName("Supplier B")//
					.supplierCode("SB")//
					.moldCount(3L)//
					.aboveTolerance(39.1)//
					.withinUpperL2Tolerance(6.9)//
					.withinL1Tolerance(20.0)//
					.withinLowerL2Tolerance(4.2)//
					.belowTolerance(29.8)//
					.nctd(0.21)//
					.nctdTrend(-1.4)//
					.build();
			content.add(item);
		}
		{
			CycTimDevItem item = CycTimDevItem.builder()//
					.supplierId(3L)//
					.supplierName("Supplier C")//
					.supplierCode("SC")//
					.moldCount(3L)//
					.aboveTolerance(24.8)//
					.withinUpperL2Tolerance(0.8)//
					.withinL1Tolerance(24.8)//
					.withinLowerL2Tolerance(24.8)//
					.belowTolerance(24.8)//
					.nctd(0.21)//
					.nctdTrend(6.4)//
					.build();
			content.add(item);
		}
		{
			CycTimDevItem item = CycTimDevItem.builder()//
					.supplierId(4L)//
					.supplierName("Supplier D")//
					.supplierCode("SD")//
					.moldCount(2L)//
					.aboveTolerance(3.4)//
					.withinUpperL2Tolerance(50.0)//
					.withinL1Tolerance(13.3)//
					.withinLowerL2Tolerance(13.3)//
					.belowTolerance(20.0)//
					.nctd(0.21)//
					.nctdTrend(-1.4)//
					.build();
			content.add(item);
		}
		{
			CycTimDevItem item = CycTimDevItem.builder()//
					.supplierId(5L)//
					.supplierName("Supplier E")//
					.supplierCode("SE")//
					.moldCount(5L)//
					.aboveTolerance(24.1)//
					.withinUpperL2Tolerance(19.9)//
					.withinL1Tolerance(8.3)//
					.withinLowerL2Tolerance(25.4)//
					.belowTolerance(22.3)//
					.nctd(0.21)//
					.nctdTrend(6.4)//
					.build();
			content.add(item);
		}

		return new CycTimDevGetOut(content, pageable, content.size());
	}

	public static CycTimDevDetailsGetOut dummyGetDetailsPage(CycTimDevDetailsGetIn input, Pageable pageable) {
		List<CycTimDevDetails> content = new ArrayList<>();
		{
			CycTimDevDetails item = CycTimDevDetails.builder()//
					.moldId(1L)//
					.moldCode("T123456")//
					.approvedCycleTime(43.1)//
					.averageCycleTime(45.5)//
					.nctd(0.45)//
					.nctdTrend(6.4)//
					.build();
			content.add(item);
		}
		{
			CycTimDevDetails item = CycTimDevDetails.builder()//
					.moldId(2L)//
					.moldCode("T123457")//
					.approvedCycleTime(30.4)//
					.averageCycleTime(29.9)//
					.nctd(0.52)//
					.nctdTrend(-2.1)//
					.build();
			content.add(item);
		}
		{
			CycTimDevDetails item = CycTimDevDetails.builder()//
					.moldId(3L)//
					.moldCode("T123458")//
					.approvedCycleTime(23.8)//
					.averageCycleTime(25.4)//
					.nctd(-0.24)//
					.nctdTrend(4.7)//
					.build();
			content.add(item);
		}
		return new CycTimDevDetailsGetOut(content, pageable, content.size(), new FltSupplier(1L, "Supplier A", "SA"), null);
	}

	private void calcCycleTimeFluctuation(CycleTimeDeviation ctd, Mold mold, String day, String prevDay, double approvedCt, double averageCt) {
		QCycleTimeDeviation table = QCycleTimeDeviation.cycleTimeDeviation;

		List<Statistics> statisticsList = BeanUtils.get(StatisticsRepository.class).findByDayAndMoldId(day, mold.getId());
		if (statisticsList.isEmpty())
			return;
		Double ctf = calcCTFluctuation(averageCt, statisticsList);
		ctd.setCtFluctuation(ctf);

		//L1
		double baseCycleTime = mold.getContractedCycleTimeToCalculation(); // 기준 사이클 타임 (contracted? ) --> 초로 계산 => second * 10
		int limit1 = mold.getCycleTimeLimit1();
		double cycleTimeL1 = mold.getCycleTimeLimit1Unit() == null || OutsideUnit.PERCENTAGE.equals(mold.getCycleTimeLimit1Unit()) ? baseCycleTime * limit1 * 0.01
				: (double) mold.getCycleTimeLimit1() * 10;// second * 10
		ctd.setL1Limit(cycleTimeL1);
		//nctf
		Double nctf = ctf / cycleTimeL1 * 100;
		ctd.setNctf(nctf);
		//calculate trend
//		if (ctd.getCtFluctuation() != null && ctd.getCtFluctuation() != 0) {
		Double prevCtd = 0d;
		Page<CycleTimeDeviation> page = BeanUtils.get(CycleTimeDeviationRepository.class).findAll(//
				new BooleanBuilder()//
						.and(table.moldId.eq(mold.getId()))//
						.and(table.timeScale.eq(TimeScale.DATE))//
						.and(table.day.loe(prevDay)), //
				PageRequest.of(0, 1, Sort.Direction.DESC, "day"));

		if (!page.isEmpty() && page.getContent().get(0).getCtFluctuation() != null) {
			prevCtd = page.getContent().get(0).getCtFluctuation();
			if (prevCtd != 0) {
				Double ctdfCurrent = ctd.getCtFluctuation() != null ? ctd.getCtFluctuation() : 0;
				Double trend = (ctdfCurrent - prevCtd) / prevCtd * 100;
				ctd.setCtfTrend(trend);
			}
		}
//		}
//		//shotCount
//		Integer shotCount = statisticsList.stream().map(s -> s.getShotCount() != null && s.getShotCount() > 0 ? s.getShotCount() : 0).reduce(0, Integer::sum);
//		ctd.setShotCount(shotCount);
	}

	private Double calcCTFluctuation(Double wact, List<Statistics> statisticsList) {
		Double val = 0d;
		Double a = statisticsList.stream().map(s -> {
			if (s.getCt() != null && s.getShotCount() != null && s.getShotCount() > 0 && s.getCt() > 0 && s.getCt() < 9999) {
				return Math.pow((s.getCt() - wact), 2) * s.getShotCount();
			}
			return 0d;
		}).reduce(0d, Double::sum);
		Integer b = statisticsList.stream()//
				.map(s -> s.getShotCount() != null && s.getShotCount() > 0 && s.getCt() != null && s.getCt() > 0 && s.getCt() < 9999 ? s.getShotCount() : 0)//
				.reduce(0, Integer::sum);
		if (b != 0)
			val = Math.sqrt(a / b);
		return val;
	}

}
