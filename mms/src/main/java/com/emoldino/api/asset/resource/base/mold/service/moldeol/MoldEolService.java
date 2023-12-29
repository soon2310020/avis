package com.emoldino.api.asset.resource.base.mold.service.moldeol;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.asset.resource.base.mold.repository.MoldRepository;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.api.common.resource.base.option.dto.RefurbishmentConfig;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.ValueUtils;

import saleson.api.endLifeCycle.MoldEndLifeCycleRepository;
import saleson.api.endLifeCycle.MoldEndLifeCycleService;
import saleson.api.endLifeCycle.MoldRefurbishmentRepository;
import saleson.api.endLifeCycle.StatisticsAccumulatingShotRepository;
import saleson.api.statistics.StatisticsService;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.ConfigOption;
import saleson.common.enumeration.PriorityType;
import saleson.common.enumeration.RefurbishmentStatus;
import saleson.model.Mold;
import saleson.model.MoldEndLifeCycle;
import saleson.model.MoldRefurbishment;
import saleson.model.Statistics;
import saleson.model.StatisticsAccumulatingShot;

@Service
public class MoldEolService {
	@Autowired
	private MoldEndLifeCycleRepository repo;

	@Transactional
	public void post(Mold mold) {
		/**
		 * Validate Mold Data
		 */
		if (mold == null || mold.getDesignedShot() == null || mold.getDesignedShot() <= 0 || mold.getLastShot() == null || mold.getLastShot() <= 0) {
			return;
		}

		// Weekly Avg. Shots = SUM(Shot Counts of The Weeks) / Number of Weeks
		Double weeklyAvgShots = calcWeeklyAverageShots(mold);
		if (weeklyAvgShots == null) {
			return;
		}

		int designedShots = mold.getDesignedShot();
		int lastShots = mold.getLastShot();
		Instant instant = DateUtils2.newInstant();

		// Daily Avg. Shots = Weekly Avg. Shots / 7d
		Long dailyAvgShots = Math.round(weeklyAvgShots / 7d);
		// Remaining Shots = Designed Shots - Last Shots
		int remainingShots = designedShots - lastShots;
		// Remaining Days = Remaining Shots / WeeklyAvgShots x 7(a week)
		Long remainingDays = Math.min(36500L, Math.round((ValueUtils.toDouble(remainingShots, 0d) / weeklyAvgShots) * 7d));
		// EOL Date = Today + Reamining Days - 1
		Instant eolAt = instant.truncatedTo(ChronoUnit.DAYS).plus(remainingDays - 1, ChronoUnit.DAYS);

		if (remainingShots < 0) {
			remainingDays = 0L;
			List<StatisticsAccumulatingShot> statisticsAccumulatingShots = BeanUtils.get(StatisticsAccumulatingShotRepository.class)
					.findAllByMoldAndRemainingShotLessThanEqualOrderByDayAsc(mold, 0);
			String day = "";
			if (!statisticsAccumulatingShots.isEmpty()) {
				for (int i = statisticsAccumulatingShots.size() - 1; i >= 0; i--) {
					day = statisticsAccumulatingShots.get(i).getDay();
					if (i == 0 || statisticsAccumulatingShots.get(i).getRemainingShot() > statisticsAccumulatingShots.get(i - 1).getRemainingShot()) {
						break;
					}
				}
			}
			if (!ObjectUtils.isEmpty(day)) {
				String zoneId = LocationUtils.getZoneIdByMold(mold);
				eolAt = DateUtils2.toInstant(day, DatePattern.yyyyMMdd, zoneId);
			}
		}

		String zoneId = LocationUtils.getZoneIdByMold(mold);
		MoldEndLifeCycle eol = repo.findFirstByMold(mold).orElse(null);
		if (eol == null) {
			eol = new MoldEndLifeCycle(mold, instant, PriorityType.LOW, null);
		}
		PriorityType priorityOld = eol.getPriority();

		// [2021-03-29] Change setting of priority by using utilization rate
		Double lm = 50.0; // Low-Medium 50% by default
		Double mh = 80.0; // Medium-High 80% by default
		Double mlDays = 12.0 * 30; // Low-Medium 8 months by default
		Double hmDays = 8.0 * 30; // Medium-High 12 months by default
		boolean calculateByDays = false;
		RefurbishmentConfig config = OptionUtils.getFieldValues(ConfigCategory.REFURBISHMENT, RefurbishmentConfig.class);
		if (config != null) {
			if (ConfigOption.UTILIZATION_RATE.equals(config.getConfigOption())) {
				lm = config.getLm();
				mh = config.getMh();
			} else if (ConfigOption.REMAINING_DAY.equals(config.getConfigOption())) {
				mlDays = config.getMlMonths() * 30;
				hmDays = config.getHmMonths() * 30;
				calculateByDays = true;
			}
		}

		if ((calculateByDays == true && remainingDays <= hmDays) || (calculateByDays == false && mold.getUtilizationRate() != null && mold.getUtilizationRate() >= mh)) {
			eol.setPriority(PriorityType.HIGH);
		} else if ((calculateByDays == true && remainingDays > hmDays && remainingDays < mlDays)
				|| (calculateByDays == false && mold.getUtilizationRate() != null && mold.getUtilizationRate() > lm && mold.getUtilizationRate() < mh)) {
			eol.setPriority(PriorityType.MEDIUM);
		} else {
			eol.setPriority(PriorityType.LOW);
		}
		if (priorityOld != eol.getPriority()) {
			eol.setIssueDate(instant);
		}
		eol.setRemainingDays(remainingDays.intValue());
		eol.setForecastShotsPerDay(dailyAvgShots.intValue());
		eol.setEndLifeAt(eolAt);
		eol.setYear(DateUtils2.format(eolAt, DatePattern.yyyy, zoneId));

		eol.setCalculationAt(instant);

		repo.save(eol);

		if (eol.getPriority() != PriorityType.HIGH && eol.getPriority() != PriorityType.MEDIUM) {
			return;
		}

		List<MoldRefurbishment> alerts = BeanUtils.get(MoldRefurbishmentRepository.class).findByLatestAndMoldEndLifeCycle(true, eol);
		MoldRefurbishment lastAlert = alerts.stream()//
				.filter(alert -> Arrays.asList(RefurbishmentStatus.END_OF_LIFECYCLE, RefurbishmentStatus.REQUESTED).contains(alert.getRefurbishmentStatus()))//
				.findFirst().orElse(null);
		if (lastAlert != null && priorityOld == eol.getPriority()) {
			lastAlert.setEndLifeAt(eol.getEndLifeAt());
			lastAlert.setForecastShotsPerDay(eol.getForecastShotsPerDay());
			lastAlert.setRemainingDays(eol.getRemainingDays());
			BeanUtils.get(MoldRefurbishmentRepository.class).save(lastAlert);
		} else {
			if (!alerts.isEmpty()) {
				alerts.stream().forEach(alert -> alert.setLatest(false));
				BeanUtils.get(MoldRefurbishmentRepository.class).saveAll(alerts);
			}

			MoldRefurbishment alert = new MoldRefurbishment();
			alert.setMoldEndLifeCycle(eol);
			alert.setMold(eol.getMold());
			alert.setEndLifeAt(eol.getEndLifeAt());
			alert.setIssueDate(eol.getIssueDate());
			alert.setPriority(eol.getPriority());
			alert.setStatus(eol.getStatus());
			alert.setYear(eol.getYear());
			alert.setDescription(eol.getDescription());
			alert.setUpdateBy(eol.getUpdateBy());
			alert.setForecastShotsPerDay(eol.getForecastShotsPerDay());
			alert.setRemainingDays(eol.getRemainingDays());
			alert.setRefurbishmentStatus(RefurbishmentStatus.END_OF_LIFECYCLE);
			alert.setFailureTime(Instant.now());
			alert.setLatest(true);
			alert.setCreatedAt(Instant.now());
			BeanUtils.get(MoldRefurbishmentRepository.class).save(alert);
			// TODO
			BeanUtils.get(MoldEndLifeCycleService.class).processAlert(alert, mold);
		}
	}

	/**
	 * The Weekly Average Shots of Maximum Recent 12 weeks
	 * SUM(Shot Counts of The Weeks) / Number of Weeks
	 * 1. Excludes this week
	 * 2. Excludes the first week (If it didn't pass 14 days(2 weeks), yet, it skips calculation.)
	 */
	private Double calcWeeklyAverageShots(Mold mold) {
		String zoneId = LocationUtils.getZoneIdByMold(mold);

		Instant startsAt = getStartsAt(mold);
		if (startsAt == null || !isEnoughDaysPassed(startsAt)) {
			return null;
		}

		List<String> targetWeeks = getTargetWeeks(startsAt, zoneId);
		if (ObjectUtils.isEmpty(targetWeeks)) {
			return null;
		}

		Double weeklyAvgShots = BeanUtils.get(MoldRepository.class).findWeeklyAvgShotCount(mold.getId(), targetWeeks);
		return weeklyAvgShots;
	}

	private Instant getStartsAt(Mold mold) {
		Instant startsAt = mold.getOperatedStartAt();
		if (startsAt != null) {
			return startsAt;
		}

		Statistics firstStat = BeanUtils.get(StatisticsService.class).firstStatistics(mold.getId());
		if (firstStat == null || ObjectUtils.isEmpty(firstStat.getLst())) {
			return null;
		}

		String zoneId = LocationUtils.getZoneIdByMold(mold);
		startsAt = DateUtils2.toInstant(firstStat.getLst(), DatePattern.yyyyMMddHHmmss, zoneId);
		return startsAt;
	}

	private boolean isEnoughDaysPassed(Instant startsAt) {
		Instant instant = DateUtils2.getInstant();
		double daysSinceStart = Math.max(0d, (double) (instant.getEpochSecond() - startsAt.getEpochSecond()) / (24 * 3600));
		return daysSinceStart >= 14d;
	}

	private List<String> getTargetWeeks(Instant startsAt, String zoneId) {
		List<String> targetWeeks = new ArrayList<>();
		String startsWeek = DateUtils2.format(startsAt.plus(Duration.ofDays(7L)), DatePattern.YYYYww, zoneId);
		Instant weekInst = DateUtils2.getInstant();
		for (int i = 0; i < 12; i++) {
			weekInst = weekInst.minus(Duration.ofDays(7));
			String week = DateUtils2.format(weekInst, DatePattern.YYYYww, zoneId);
			if (week.compareTo(startsWeek) < 0) {
				break;
			}
			targetWeeks.add(week);
		}
		return targetWeeks;
	}

}
