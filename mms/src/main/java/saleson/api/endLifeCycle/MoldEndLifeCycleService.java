package saleson.api.endLifeCycle;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.asset.resource.base.mold.service.moldeol.MoldEolService;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.ReflectionUtils;
import com.emoldino.framework.util.TranUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import lombok.extern.slf4j.Slf4j;
import saleson.api.chart.payload.ChartPayload;
import saleson.api.endLifeCycle.payload.EndLifeCyclePayload;
import saleson.api.mold.MoldRepository;
import saleson.api.mold.MoldService;
import saleson.api.mold.payload.MoldPayload;
import saleson.api.statistics.StatisticsRepository;
import saleson.api.user.UserAlertRepository;
import saleson.api.user.UserService;
import saleson.common.constant.SpecialSortProperty;
import saleson.common.enumeration.AlertType;
import saleson.common.enumeration.EndOfLifeCycleStatus;
import saleson.common.notification.MailService;
import saleson.common.util.DateUtils;
import saleson.common.util.ExcelUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.model.LogUserAlert;
import saleson.model.Mold;
import saleson.model.MoldEndLifeCycle;
import saleson.model.MoldRefurbishment;
import saleson.model.Statistics;
import saleson.model.StatisticsAccumulatingShot;
import saleson.model.User;
import saleson.model.UserAlert;
import saleson.model.data.MoldEndLifeCycleChartData;
import saleson.model.data.MoldEndLifeCycleChartResponse;
import saleson.service.transfer.LogUserAlertRepository;

@Slf4j
@Service
public class MoldEndLifeCycleService {
	@Autowired
	private MoldEndLifeCycleRepository repo;

	public Page<MoldEndLifeCycle> findAll(Predicate predicate, Pageable pageable, String accumulatedShotFilter) {
		String[] properties = { "" };
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
		});

		Page<MoldEndLifeCycle> page;

		if (properties[0].startsWith(SpecialSortProperty.priority)) {
			List<MoldEndLifeCycle> list = repo.findAllOrderByPriority(predicate, pageable);
			page = new PageImpl<>(list, pageable, repo.count(predicate));
		} else if (properties[0].contains(SpecialSortProperty.moldAccumulatedShotSort)) {
			List<MoldEndLifeCycle> list = repo.findAllOrderByAccumulatedShot(predicate, pageable);
			page = new PageImpl<>(list, pageable, repo.count(predicate));
		} else if (SpecialSortProperty.moldSortProperties.contains(properties[0])) {
			List<MoldEndLifeCycle> list = repo.findAllOrderByPart(predicate, pageable);
			page = new PageImpl<>(list, pageable, repo.count(predicate));
		} else if (properties[0].contains(SpecialSortProperty.operatingStatus)) {
			List<MoldEndLifeCycle> list = repo.findAllOrderByOperatingStatus(predicate, pageable);
			page = new PageImpl<>(list, pageable, repo.count(predicate));
		} else if (SpecialSortProperty.moldCounterStatusProperties.contains(properties[0])) {
			List<MoldEndLifeCycle> list = repo.findAllOrderByStatus(predicate, pageable);
			page = new PageImpl<>(list, pageable, repo.count(predicate));
		} else {
			page = repo.findAll(predicate, pageable);
		}
		BeanUtils.get(MoldService.class).loadAccumulatedShot(page.getContent().stream().map(MoldEndLifeCycle::getMold).filter(Objects::nonNull).collect(Collectors.toList()),
				accumulatedShotFilter);
		return page;
	}

	public MoldEndLifeCycle findById(Long id) {
		return repo.findById(id).orElse(null);
	}

	@Transactional
	public MoldEndLifeCycle save(MoldEndLifeCycle moldEndLifeCycle, EndLifeCyclePayload payload) {
		if (payload == null) {
			repo.save(moldEndLifeCycle);
			return moldEndLifeCycle;
		}
		if (moldEndLifeCycle.getStatus() == null) {
			moldEndLifeCycle.setStatus(EndOfLifeCycleStatus.DISMISS);
		}
		if (moldEndLifeCycle.getUpdateBy() == null) {
			User updateBy = BeanUtils.get(UserService.class).findById(SecurityUtils.getUserId());
			moldEndLifeCycle.setUpdateBy(updateBy);
		}
		return moldEndLifeCycle;
	}

	@Transactional(propagation = Propagation.NEVER)
	public void procAll() {
		JobUtils.runIfNotRunning(ReflectionUtils.toShortName(MoldEndLifeCycleService.class, "ALL"), new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true), () -> {
			DataUtils.runBatch(MoldRepository.class, null, Sort.by("id"), 100, true, (mold) -> {
				BeanUtils.get(MoldEndLifeCycleService.class).createOrUpdateMoldEndLifeCycle(mold, false);
			});
		});
	}

	@Transactional
	public void createOrUpdateMoldEndLifeCycle(Mold mold, boolean updateByPreset) {
		BeanUtils.get(MoldEolService.class).post(mold);

//		if (mold == null || mold.getDesignedShot() == null || mold.getDesignedShot() == 0) {
//			return;
//		}
//
//		MoldEndLifeCycle moldEndLifeCycle = repo.findFirstByMold(mold).orElse(null);
//		if (moldEndLifeCycle == null) {
//			moldEndLifeCycle = new MoldEndLifeCycle(mold, Instant.now(), PriorityType.LOW, null);
//		}
//
//		Integer lastShot = mold.getLastShot() != null ? mold.getLastShot() : 0;
//		if (lastShot == 0) {
//			return;
//		}
//
//		Integer remainingShot = mold.getDesignedShot() - lastShot;
//		Instant operatedStartAt = mold.getOperatedStartAt();
//
//		if (operatedStartAt == null) {
//			Statistics first = BeanUtils.get(StatisticsService.class).firstStatistics(mold.getId());
//
//			if (first != null && !StringUtils.isEmpty(first.getLst())) {
//				operatedStartAt = DateUtils.getInstant(first.getLst(), DateUtils.DEFAULT_DATE_FORMAT);
//			}
//		}
//		if (operatedStartAt == null) {
//			operatedStartAt = Instant.now().plus(-7, ChronoUnit.DAYS);
//		}
//
//		Double dualDate = (Double.valueOf(Instant.now().getEpochSecond() - operatedStartAt.getEpochSecond())) / (24 * 3600);
//		//calculator new dueDate
//		List<StatisticsPreset> statisticsPresets = BeanUtils.get(StatisticsPresetRepository.class).findAllByMoldId(mold.getId());
//		if (!statisticsPresets.isEmpty()) {
//			Integer totalMissingDays = statisticsPresets.stream().filter(s -> s.getMissingDays() != null).map(s -> s.getMissingDays()).reduce(0, Integer::sum);
//			dualDate += totalMissingDays;
//		}
//
//		if (dualDate == 0) {
//			log.warn("Mold dualDate is zero: " + mold.getEquipmentCode());
//			return;
//		}
//		Integer forecastShotsPerDay = ((Long) Math.round(lastShot / dualDate)).intValue();
//		forecastShotsPerDay = forecastShotsPerDay == 0 ? 1 : forecastShotsPerDay;
//
//		Double remainingDays = Double.valueOf(remainingShot) / forecastShotsPerDay;
//		Long remainingDaysRound = Math.round(remainingDays) + (remainingShot % forecastShotsPerDay > 0 ? 1 : 0);
//		remainingDaysRound = remainingDaysRound > 36500 ? 36500 : remainingDaysRound;
//
//		Instant endLifeAt = Instant.now().truncatedTo(ChronoUnit.DAYS).plus(remainingDaysRound - 1, ChronoUnit.DAYS);
//		//for remaining days <0
//		if (remainingShot < 0) {
//			List<StatisticsAccumulatingShot> statisticsAccumulatingShots = BeanUtils.get(StatisticsAccumulatingShotRepository.class)
//					.findAllByMoldAndRemainingShotLessThanEqualOrderByDayAsc(mold, 0);
//			String day = "";
//			if (!statisticsAccumulatingShots.isEmpty()) {
//				for (int i = statisticsAccumulatingShots.size() - 1; i >= 0; i--) {
//					day = statisticsAccumulatingShots.get(i).getDay();
//					if (i == 0 || statisticsAccumulatingShots.get(i).getRemainingShot() > statisticsAccumulatingShots.get(i - 1).getRemainingShot()) {
//						break;
//					}
//				}
//			}
//			if (!StringUtils.isEmpty(day)) {
//				endLifeAt = DateUtils.getInstant(day + "000000", DateUtils.DEFAULT_DATE_FORMAT);
//			}
//		}
//
//		PriorityType priorityTypeOld = moldEndLifeCycle.getPriority();
//
//		// [2021-03-29] Change setting of priority by using utilization rate
//		Double lm = 50.0; // Low-Medium 50% by default
//		Double mh = 80.0; // Medium-High 80% by default
//		Double mlDays = 12.0 * 30; // Low-Medium 8 months by default
//		Double hmDays = 8.0 * 30; // Medium-High 12 months by default
//		boolean calculateByDays = false;
//		if (OptionUtils.isEnabled(ConfigCategory.REFURBISHMENT)) {
//			RefurbishmentConfig config = OptionUtils.getFieldValues(ConfigCategory.REFURBISHMENT, RefurbishmentConfig.class);
//			if (config != null) {
//				if (ConfigOption.UTILIZATION_RATE.equals(config.getConfigOption())) {
//					lm = config.getLm();
//					mh = config.getMh();
//				} else if (ConfigOption.REMAINING_DAY.equals(config.getConfigOption())) {
//					mlDays = config.getMlMonths() * 30;
//					hmDays = config.getHmMonths() * 30;
//					calculateByDays = true;
//				}
//			}
//		}
//
//		if ((calculateByDays == true && remainingDays <= hmDays) || (calculateByDays == false && mold.getUtilizationRate() != null && mold.getUtilizationRate() >= mh)) {
//			moldEndLifeCycle.setPriority(PriorityType.HIGH);
//		} else if ((calculateByDays == true && remainingDays > hmDays && remainingDays < mlDays)
//				|| (calculateByDays == false && mold.getUtilizationRate() != null && mold.getUtilizationRate() > lm && mold.getUtilizationRate() < mh)) {
//			moldEndLifeCycle.setPriority(PriorityType.MEDIUM);
//		} else {
//			moldEndLifeCycle.setPriority(PriorityType.LOW);
//		}
//		if (priorityTypeOld != moldEndLifeCycle.getPriority()) {
//			moldEndLifeCycle.setIssueDate(Instant.now());
//		}
//		moldEndLifeCycle.setRemainingDays(remainingDaysRound.intValue());
//		moldEndLifeCycle.setForecastShotsPerDay(forecastShotsPerDay);
//		moldEndLifeCycle.setEndLifeAt(endLifeAt);
//		moldEndLifeCycle.setYear(DateUtils.getYear(endLifeAt));
//
//		moldEndLifeCycle.setCalculationAt(Instant.now());
//
//		repo.save(moldEndLifeCycle);
//		//notion for
//		if (moldEndLifeCycle.getPriority() == PriorityType.HIGH || moldEndLifeCycle.getPriority() == PriorityType.MEDIUM) {
//			List<MoldRefurbishment> lifeCycleHistoryList = BeanUtils.get(MoldRefurbishmentRepository.class).findByLatestAndMoldEndLifeCycle(true, moldEndLifeCycle);
//			MoldRefurbishment mr = lifeCycleHistoryList.stream()
//					.filter(o -> Arrays.asList(RefurbishmentStatus.END_OF_LIFECYCLE, RefurbishmentStatus.REQUESTED).contains(o.getRefurbishmentStatus())).findFirst().orElse(null);
//			if (priorityTypeOld != moldEndLifeCycle.getPriority() || mr == null) {
//				if (!lifeCycleHistoryList.isEmpty()) {
//					lifeCycleHistoryList.stream().forEach(o -> o.setLatest(false));
//					BeanUtils.get(MoldRefurbishmentRepository.class).saveAll(lifeCycleHistoryList);
//				}
//
//				MoldRefurbishment mHistory = new MoldRefurbishment();
////				mHistory.setId(null);
////				mHistory.setMoldEndLifeCycleId(moldEndLifeCycle.getId());
//				mHistory.setMoldEndLifeCycle(moldEndLifeCycle);
//
//				mHistory.setMold(moldEndLifeCycle.getMold());
//				mHistory.setEndLifeAt(moldEndLifeCycle.getEndLifeAt());
//				mHistory.setIssueDate(moldEndLifeCycle.getIssueDate());
//				mHistory.setPriority(moldEndLifeCycle.getPriority());
//				mHistory.setStatus(moldEndLifeCycle.getStatus());
//				mHistory.setYear(moldEndLifeCycle.getYear());
//				mHistory.setDescription(moldEndLifeCycle.getDescription());
//				mHistory.setUpdateBy(moldEndLifeCycle.getUpdateBy());
//				mHistory.setForecastShotsPerDay(moldEndLifeCycle.getForecastShotsPerDay());
//				mHistory.setRemainingDays(moldEndLifeCycle.getRemainingDays());
//				mHistory.setRefurbishmentStatus(RefurbishmentStatus.END_OF_LIFECYCLE);
//
//				mHistory.setFailureTime(Instant.now());
//				mHistory.setLatest(true);
//				mHistory.setCreatedAt(Instant.now());
//				BeanUtils.get(MoldRefurbishmentRepository.class).save(mHistory);
//				processAlert(mHistory, mold);
//			} else {
//				mr.setEndLifeAt(moldEndLifeCycle.getEndLifeAt());
//				mr.setForecastShotsPerDay(moldEndLifeCycle.getForecastShotsPerDay());
//				mr.setRemainingDays(moldEndLifeCycle.getRemainingDays());
//				BeanUtils.get(MoldRefurbishmentRepository.class).save(mr);
//			}
//		} else if (updateByPreset) {
//			//close Refurbishment
//			List<MoldRefurbishment> existing = BeanUtils.get(MoldRefurbishmentRepository.class).findByLatestAndMoldEndLifeCycle(true, moldEndLifeCycle);
//			existing.forEach(mc -> {
//				mc.setLastChecked(false);
//				if (!RefurbishmentStatus.COMPLETED.equals(mc.getRefurbishmentStatus())) {
//					mc.setLatest(false);
//				}
//			});
//			BeanUtils.get(MoldRefurbishmentRepository.class).saveAll(existing);
//			if (!existing.isEmpty()) {
//				MoldRefurbishment moldRefurbishment = existing.stream().filter(e -> !RefurbishmentStatus.COMPLETED.equals(e.getRefurbishmentStatus())).findFirst()
//						.orElse(existing.get(0));
//				moldRefurbishment.setLastChecked(true);
//				moldRefurbishment.setLatest(true);
//				if (!RefurbishmentStatus.COMPLETED.equals(moldRefurbishment.getRefurbishmentStatus())) {
//
//					moldRefurbishment.setRefurbishmentStatus(RefurbishmentStatus.COMPLETED);
//					String memo = "Completed by reset - " + SecurityUtils.getName();
//					moldRefurbishment.setMemo(moldRefurbishment.getMemo() != null && moldRefurbishment.getMemo() != "" ? moldRefurbishment.getMemo() + "\n" + memo : memo);
//				}
//
//				BeanUtils.get(MoldRefurbishmentRepository.class).save(moldRefurbishment);
//			}
//		}
	}

	public List<MoldEndLifeCycleChartData> getMoldEndLifeCycleChartData(Long id, ChartPayload payload) {
		List<MoldEndLifeCycleChartData> chartDataList = new ArrayList<>();
		//TODO: create chart
//        Instant startDate = payload.getStartTime() != null ? Instant.ofEpochSecond(payload.getStartTime()) : null;
//        Instant endDate = payload.getEndTime() != null ? Instant.ofEpochSecond(payload.getEndTime()) : null;
		int durationDate = 14;
		MoldEndLifeCycle moldEndLifeCycle = repo.findById(id).orElse(null);
		if (moldEndLifeCycle != null && moldEndLifeCycle.getEndLifeAt() != null) {
			//create chart
			Mold mold = moldEndLifeCycle.getMold();
			Instant startDate = moldEndLifeCycle.getEndLifeAt().minus((durationDate - 1), ChronoUnit.DAYS);
			String startDay = DateUtils.getDay(startDate);
			List<Statistics> statisticsList = new ArrayList<>();
			if (startDate.isBefore(moldEndLifeCycle.getUpdatedAt())) {
				String currentDay = DateUtils.getDay(moldEndLifeCycle.getUpdatedAt());
				statisticsList = BeanUtils.get(StatisticsRepository.class).findByDayBetweenAndMoldId(startDay, currentDay, moldEndLifeCycle.getMoldId());

			}
			Integer designedShot = mold.getDesignedShot() != null && mold.getDesignedShot() > 0 ? mold.getDesignedShot() : 0;

			for (int i = 1; i <= durationDate; i++) {
				Instant dateCurrent = moldEndLifeCycle.getEndLifeAt().plus(-durationDate + i, ChronoUnit.DAYS);
				String title = DateUtils.getDay(dateCurrent);
				Integer accumulatingShot = designedShot - moldEndLifeCycle.getForecastShotsPerDay() * (durationDate - i);
				accumulatingShot = accumulatingShot < 0 ? 0 : (accumulatingShot > designedShot ? designedShot : accumulatingShot);

				if (dateCurrent.isBefore(moldEndLifeCycle.getCalculationAt())) {
					List<Integer> statisticsList1 = statisticsList.stream().filter(s -> title.equals(s.getDay()) && s.getSc() != null).map(s -> s.getSc())
							.collect(Collectors.toList());
					if (!statisticsList1.isEmpty()) {
						accumulatingShot = statisticsList1.get(statisticsList1.size() - 1);
					} else {
						accumulatingShot = null;
					}
				}
				Integer remainingShot = accumulatingShot != null ? (designedShot - accumulatingShot) : null;

				MoldEndLifeCycleChartData mChart = new MoldEndLifeCycleChartData(title, dateCurrent.getEpochSecond(), accumulatingShot, remainingShot);
				chartDataList.add(mChart);
			}
		}

		return chartDataList;
	}

	public ByteArrayOutputStream exportExcelDataMolds(List<Long> ids, Integer timezoneOffsetClient, Pageable pageable) {
		List<MoldEndLifeCycle> moldList;
		if (ids == null || ids.size() == 0) {
			moldList = repo.findAllByOrderByIdDesc();
		} else {
			if (pageable != null && pageable.getSort() != null) {
				Pageable pageableNew = Pageable.unpaged();
				if (pageable.getSort() != null) {
					pageableNew = PageRequest.of(0, Integer.MAX_VALUE, pageable.getSort());
				}
				MoldPayload payload = new MoldPayload();
				payload.setIds(ids);
				Page<MoldEndLifeCycle> pageContent = findAll(payload.getPredicate(), pageableNew, null);
				moldList = pageContent.getContent();
			} else
				moldList = repo.findByIdInOrderByIdDesc(ids);
		}

		return BeanUtils.get(ExcelUtils.class).exportExcelToolingEndLifeCycleDetail(moldList);
	}

	public void processAlert(MoldRefurbishment moldEndLifeCycle, Mold mold) {

		// 3. Generate log user alert
		/*
		List<User> supplierList = subTierService.getTierSuppliers(mold);
		
		if (serverName.equalsIgnoreCase("dyson")) {
		    List<User> engineers = mold.getEngineers();
		    engineers.forEach(engineer -> {
		        if (!supplierList.contains(engineer)) supplierList.add(engineer);
		    });
		} else {
		    List<User> oemList = userRepository.findByCompanyTypeAndOrderByName(CompanyType.IN_HOUSE.name());
		    oemList.forEach(oem -> {
		        if (!supplierList.contains(oem)) supplierList.add(oem);
		    });
		}
		*/
		List<User> supplierList = BeanUtils.get(MoldService.class).getSupplierListOfMold(mold);

		List<UserAlert> userAlertList = BeanUtils.get(UserAlertRepository.class).findByUserInAndAlertType(supplierList, AlertType.REFURBISHMENT);
		// TODO will be replaced with new one (by Aaron)
		Map<User, List<AlertType>> userAlertMap = BeanUtils.get(MailService.class).getUserAlertListMap(userAlertList);
		List<LogUserAlert> logUserAlerts = BeanUtils.get(MoldService.class).generateLogUserAlert(userAlertMap, Arrays.asList(moldEndLifeCycle), true);
		BeanUtils.get(LogUserAlertRepository.class).saveAll(logUserAlerts);

		/*
		List<UserAlert> toSendMailUserAlertList = userAlertList.stream().filter(x -> x.getEmail() == true).collect(Collectors.toList());
		Map<User, List<AlertType>> toSendMailUserAlertMap = mailService.getUserAlertListMap(toSendMailUserAlertList);
		mailService.sendMailForEachUser(toSendMailUserAlertMap, PeriodType.REAL_TIME,  Arrays.asList(moldEndLifeCycle));
		*/
	}

	/**
	 * history total shot at day using for chart shot on end of time cycle
	 */
	@Transactional(propagation = Propagation.NEVER)
	public void procAccumulativeShotAll() {
		JobUtils.runIfNotRunning(ReflectionUtils.toShortName(MoldEndLifeCycleService.class, "ACC_SHOT"), new JobOptions().setClustered(true).setAccessSummaryLogEnabled(true),
				() -> {
					DataUtils.runBatch(MoldRepository.class, new BooleanBuilder(), Sort.by("id"), 100, true, mold -> {
						if (mold.getLastShot() == null || mold.getDesignedShot() == null) {
							return;
						}
						TranUtils.doNewTran(() -> {
							Integer remainingShot = mold.getDesignedShot() - mold.getLastShot();
							StatisticsAccumulatingShot shot = new StatisticsAccumulatingShot(mold, mold.getLastShot(), remainingShot, Instant.now().minus(1, ChronoUnit.DAYS));
							//check exit
							StatisticsAccumulatingShot old = BeanUtils.get(StatisticsAccumulatingShotRepository.class)
									.findFirstByMoldAndDay(shot.getMold(), DateUtils.getDay(Instant.now().minus(1, ChronoUnit.DAYS))).orElse(null);
							if (old == null) {
								BeanUtils.get(StatisticsAccumulatingShotRepository.class).save(shot);
							} else {
								old.setAccumulatingShot(shot.getAccumulatingShot());
								old.setRemainingShot(shot.getRemainingShot());
								old.setDesignedShot(shot.getDesignedShot());
								old.setWorkingInDay(shot.getWorkingInDay());
								BeanUtils.get(StatisticsAccumulatingShotRepository.class).save(old);
							}
						});
					});
				});
	}

	/**
	 * cal total shot created to time(day)
	 * @param day
	 */
	private void processStatisticsAccumulatingShotPer(String day) {
		Long total = BeanUtils.get(StatisticsRepository.class).countStatisticsAccumulatingShotByDay(day);
		int pageSize = 100;
		int numPage = Long.valueOf(total / pageSize + (total % pageSize == 0 ? 0 : 1)).intValue();
		log.info("Start [processStatisticsAccumulatingShot] day " + day + " size " + total);
		Instant fromT = Instant.now();
		int addNew = 0;
		int update = 0;
		for (int page = 0; page < numPage; page++) {
			List<StatisticsAccumulatingShot> shotList = BeanUtils.get(StatisticsRepository.class).statisticsAccumulatingShotByDay(null, day, PageRequest.of(page, pageSize));
			for (int i = 0; i < shotList.size(); i++) {
				StatisticsAccumulatingShot s = shotList.get(i);
				//check exit
				if (s.getMold().getCreatedAt() == null || DateUtils.getInstant(day + "0000", "yyyyMMddHHmm").isBefore(s.getMold().getCreatedAt().truncatedTo(ChronoUnit.DAYS))) {
					continue;
				}

				StatisticsAccumulatingShot old = BeanUtils.get(StatisticsAccumulatingShotRepository.class).findFirstByMoldAndDay(s.getMold(), day).orElse(null);
				if (old == null) {
					BeanUtils.get(StatisticsAccumulatingShotRepository.class).save(s);
					addNew++;
				} else {
					old.setAccumulatingShot(s.getAccumulatingShot());
					old.setRemainingShot(s.getRemainingShot());
					old.setDesignedShot(s.getDesignedShot());
					old.setWorkingInDay(s.getWorkingInDay());
					BeanUtils.get(StatisticsAccumulatingShotRepository.class).save(old);
					update++;
				}

			}
		}
		log.info("End [processStatisticsAccumulatingShot] day " + day + " dua time " + (Instant.now().getEpochSecond() - fromT.getEpochSecond()) + "s new: " + addNew + " update : "
				+ update);
	}

	public void jobStatisticsAccumulatingShot() {
		Instant dayStatistics = Instant.now().minus(1, ChronoUnit.DAYS);
		processStatisticsAccumulatingShotPer(DateUtils.getDay(dayStatistics));
	}

	public void initStatisticsAccumulatingShot(Boolean allTime, String fromDay) {
		log.info("Start [initStatisticsAccumulatingShot] allTime: " + allTime + " fromDay: " + fromDay);
		Instant fromTime = Instant.now();
		if (allTime != true && StringUtils.isEmpty(fromDay)) {
//            jobStatisticsAccumulatingShot();
			procAccumulativeShotAll();
			return;
		} else {
			if (allTime) {
				Statistics statistics = BeanUtils.get(StatisticsRepository.class).findFirstByDayIsNotNullOrderByDayAsc().orElse(null);
				if (statistics != null) {
					fromDay = statistics.getDay();
				}
			}
		}
		if (StringUtils.isEmpty(fromDay)) {
			jobStatisticsAccumulatingShot();
			return;
		}
		Instant fromDate = DateUtils.getInstant(fromDay + "0000", "yyyyMMddHHmm");
		for (Instant date = fromDate; date.isBefore(Instant.now()); date = date.plus(1, ChronoUnit.DAYS)) {
			processStatisticsAccumulatingShotPer(DateUtils.getDay(date));
		}
		log.info("End [initStatisticsAccumulatingShot] dua time : " + (Instant.now().getEpochSecond() - fromTime.getEpochSecond()) + "s");
	}

	public MoldEndLifeCycleChartResponse getMoldEndLifeCycleChartDataFull(Long id) {
		MoldEndLifeCycleChartResponse response = new MoldEndLifeCycleChartResponse();
		MoldEndLifeCycle moldEndLifeCycle = repo.findById(id).orElse(null);
		if (moldEndLifeCycle == null || moldEndLifeCycle.getMold() == null) {
			return response;
		}

		Mold mold = BeanUtils.get(MoldRepository.class).findById(moldEndLifeCycle.getMoldId()).orElse(null);
		String dayEndTime = moldEndLifeCycle.getEndLifeAt() != null ? DateUtils.getDay(moldEndLifeCycle.getEndLifeAt()) : null;
		List<StatisticsAccumulatingShot> shotList = dayEndTime == null ? BeanUtils.get(StatisticsAccumulatingShotRepository.class).findAllByMoldOrderByDayAsc(mold)
				: BeanUtils.get(StatisticsAccumulatingShotRepository.class).findAllByMoldAndDayLessThanEqualOrderByDayAsc(mold, dayEndTime);
		List<MoldEndLifeCycleChartData> lifeCycleChartData = shotList.stream().map(s -> s.convertMoldEndLifeCycleChartData(mold.getDesignedShot())).collect(Collectors.toList());
		response.setMoldEndLifeCycleChartDataList(lifeCycleChartData);
		response.setTotal(Long.valueOf(lifeCycleChartData.size()));
		if (moldEndLifeCycle != null) {
			response.setEndLifeAt(moldEndLifeCycle.getEndLifeAt());
		}
		if (shotList.size() > 0) {
			StatisticsAccumulatingShot last = shotList.get(shotList.size() - 1);
			response.setAccumulatingShot(last.getAccumulatingShot());
			response.setRemainingShot(last.getRemainingShot() > 0 ? last.getRemainingShot() : 0);
			response.setLastDate(last.getDay());
		}

		return response;

	}

}
