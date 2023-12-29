package com.emoldino.api.integration.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.QDataAcceleration;
import com.emoldino.api.integration.resource.base.ai.dto.AiStatistics;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.Closure1ParamNoReturn;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.api.mold.MoldRepository;
import saleson.model.Mold;

@Component
public class AiUtils {
	public static void procFetch(BooleanBuilder filter, Closure1ParamNoReturn<Mold> closure) {
		int moldCnt = 0;
		int pageNo = 0;
		Page<Mold> page;
		while (moldCnt++ < 10000 //
				&& !(page = BeanUtils.get(MoldRepository.class).findAll(filter, PageRequest.of(pageNo++, 100, Direction.ASC, "operatedStartAt"))).isEmpty()) {
			for (Mold mold : page.getContent()) {
				if (ObjectUtils.isEmpty(mold.getEquipmentCode()) || ObjectUtils.isEmpty(mold.getCounterId())) {
					continue;
				}
				closure.execute(mold);
			}
		}
	}

	public static void procFetch(Closure1ParamNoReturn<Mold> closure) {
		int moldCnt = 0;
		int pageNo = 0;
		Page<Mold> page;
		while (moldCnt++ < 10000 //
				&& !(page = BeanUtils.get(MoldRepository.class).findAll(PageRequest.of(pageNo++, 100, Direction.ASC, "operatedStartAt"))).isEmpty()) {
			for (Mold mold : page.getContent()) {
				if (ObjectUtils.isEmpty(mold.getEquipmentCode())) {
					continue;
				}
				closure.execute(mold);
			}
		}
	}

	public static Integer getSensorGeneration(String sensorId) {
		if (sensorId.startsWith("CMS")) {
			return 1;
		} else if (sensorId.startsWith("NCM")) {
			return 2;
		} else if (sensorId.startsWith("EMA")) {
			return 3;
		}
		return 3;
	}

	public static List<AiStatistics> getAiStatistics(Long moldId, Instant fromTime, Instant toTime) {
		return BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(AiStatistics.class, // 
						Q.statistics.id, //
						Q.statisticsExt.shotCountCtt, //
						Q.statistics.ct, //
						Q.statistics.tav, //
						Q.statistics.hour, //
						Q.statistics.tff))//
				.from(Q.statistics)//
				.leftJoin(Q.statisticsExt)//
				.on(Q.statisticsExt.cdataId.eq(Q.statistics.cdataId))//
				.where(Q.statistics.tff.isNotNull() //
						.and(Q.statistics.tff.goe(DateUtils2.format(fromTime, DatePattern.yyyyMMddHHmmss, Zone.GMT))) //
						.and(Q.statistics.tff.loe(DateUtils2.format(toTime, DatePattern.yyyyMMddHHmmss, Zone.GMT))) //
						.and(Q.statistics.moldId.eq(moldId)))//
				.orderBy(Q.statistics.tff.asc()) //
				.fetch();
	}

	public static Instant getLastTff() {
		String lastTff = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.statistics.tff) //
				.from(Q.statistics) //
				.where(Q.statistics.tff.isNotNull()) //
				.orderBy(Q.statistics.tff.desc())//
				.fetchFirst();

		if (ObjectUtils.isEmpty(lastTff)) {
			return null;
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		LocalDateTime time = LocalDateTime.parse(lastTff, formatter);

		return time.toInstant(ZoneOffset.UTC);
	}

	public static Instant getLastMeasurementTime() {
		QDataAcceleration table = QDataAcceleration.dataAcceleration;
		String lastTime = BeanUtils.get(JPAQueryFactory.class) //
				.select(table.measurementDate) //
				.from(table) //
				.orderBy(table.measurementDate.desc()) //
				.fetchFirst();

		if (ObjectUtils.isEmpty(lastTime)) {
			return null;
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		LocalDateTime time = LocalDateTime.parse(lastTime, formatter);

		return time.toInstant(ZoneOffset.UTC);
	}

	/**
	 * It calculate minus Weeks at Instant
	 * @param instant	The Source Instant
	 * @param months	The months for calculating plus
	 * @param zoneId	Based TimeZone for recognize the Instant Value
	 * @return
	 */
	public static Instant minusWeeks(Instant instant, int weeks, String zoneId) {
		LogicUtils.assertNotEmpty(zoneId, "zoneId");

		ZoneId zone = ZoneId.of(zoneId);
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
		Instant value = localDateTime.minusWeeks(weeks).atZone(zone).toInstant();
		return value;
	}

	/**
	 * It calculate minus Months at Instant
	 * @param instant	The Source Instant
	 * @param months	The months for calculating plus
	 * @param zoneId	Based TimeZone for recognize the Instant Value
	 * @return
	 */
	public static Instant minusMonths(Instant instant, int months, String zoneId) {
		LogicUtils.assertNotEmpty(zoneId, "zoneId");

		ZoneId zone = ZoneId.of(zoneId);
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
		Instant value = localDateTime.minusMonths(months).atZone(zone).toInstant();
		return value;
	}

	/**
	 * It calculate minus Years at Instant
	 * @param instant	The Source Instant
	 * @param months	The months for calculating plus
	 * @param zoneId	Based TimeZone for recognize the Instant Value
	 * @return
	 */
	public static Instant minusYears(Instant instant, int years, String zoneId) {
		LogicUtils.assertNotEmpty(zoneId, "zoneId");

		ZoneId zone = ZoneId.of(zoneId);
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
		Instant value = localDateTime.minusYears(years).atZone(zone).toInstant();
		return value;
	}

	/**
	 * It calculate plus Weeks at Instant
	 * @param instant	The Source Instant
	 * @param months	The months for calculating plus
	 * @param zoneId	Based TimeZone for recognize the Instant Value
	 * @return
	 */
	public static Instant plusWeeks(Instant instant, int weeks, String zoneId) {
		LogicUtils.assertNotEmpty(zoneId, "zoneId");

		ZoneId zone = ZoneId.of(zoneId);
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
		Instant value = localDateTime.plusWeeks(weeks).atZone(zone).toInstant();
		return value;
	}

	/**
	 * It calculate plus Months at Instant
	 * @param instant	The Source Instant
	 * @param months	The months for calculating plus
	 * @param zoneId	Based TimeZone for recognize the Instant Value
	 * @return
	 */
	public static Instant plusMonths(Instant instant, int months, String zoneId) {
		LogicUtils.assertNotEmpty(zoneId, "zoneId");

		ZoneId zone = ZoneId.of(zoneId);
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
		Instant value = localDateTime.plusMonths(months).atZone(zone).toInstant();
		return value;
	}

	/**
	 * It calculate plus Years at Instant
	 * @param instant	The Source Instant
	 * @param months	The months for calculating plus
	 * @param zoneId	Based TimeZone for recognize the Instant Value
	 * @return
	 */
	public static Instant plusYears(Instant instant, int years, String zoneId) {
		LogicUtils.assertNotEmpty(zoneId, "zoneId");

		ZoneId zone = ZoneId.of(zoneId);
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
		Instant value = localDateTime.plusYears(years).atZone(zone).toInstant();
		return value;
	}

	public static Instant minusTime(String periodStr, Instant time) {
		if (ObjectUtils.isEmpty(periodStr) || ObjectUtils.isEmpty(time)) {
			return time;
		}

		int period = Integer.parseInt(periodStr.substring(0, periodStr.length() - 1));

		switch (periodStr.charAt(periodStr.length() - 1)) {
		case 'H':
			time = resetTimeToZero(time.minus(period, ChronoUnit.HOURS), 3);
			break;
		case 'D':
			time = resetTimeToZero(time.minus(period, ChronoUnit.DAYS), 4);
			break;
		case 'W':
			time = resetTimeToZero(AiUtils.minusWeeks(time, period, Zone.SYS), 4);
			break;
		case 'M':
			time = resetTimeToZero(AiUtils.minusMonths(time, period, Zone.SYS), 4);
			break;
		case 'Y':
			time = resetTimeToZero(AiUtils.minusYears(time, period, Zone.SYS), 4);
			break;
		default:
			time = resetTimeToZero(time.minus(period, ChronoUnit.HOURS), 3);
			break;
		}

		return time;
	}

	public static Instant plusTime(String periodStr, Instant time) {
		if (ObjectUtils.isEmpty(periodStr) || ObjectUtils.isEmpty(time)) {
			return time;
		}

		int period = Integer.parseInt(periodStr.substring(0, periodStr.length() - 1));

		switch (periodStr.charAt(periodStr.length() - 1)) {
		case 'H':
			time = resetTimeToZero(time.plus(period, ChronoUnit.HOURS), 3);
			break;
		case 'D':
			time = resetTimeToZero(time.plus(period, ChronoUnit.DAYS), 4);
			break;
		case 'W':
			time = resetTimeToZero(AiUtils.plusWeeks(time, period, Zone.SYS), 4);
			break;
		case 'M':
			time = resetTimeToZero(AiUtils.plusMonths(time, period, Zone.SYS), 4);
			break;
		case 'Y':
			time = resetTimeToZero(AiUtils.plusYears(time, period, Zone.SYS), 4);
			break;
		default:
			time = resetTimeToZero(time.plus(period, ChronoUnit.HOURS), 3);
			break;
		}

		return time;
	}

	/**
	 * It reset time to zero 
	 * @param instant The Source Instant
	 * @param level The Level to make zero <br>
	 * Level 1: nano <br>
	 * Level 2: nano, second <br>
	 * Level 3: nano, second, minute <br>
	 * Level 4: nano, second, minute, hour <br>
	 * @return Instant
	 */
	public static Instant resetTimeToZero(Instant time, int level) {

		LogicUtils.assertNotEmpty(Zone.SYS, "zoneId");
		ZoneId zone = ZoneId.of(Zone.SYS);

		if (level == 1) {
			return LocalDateTime.ofInstant(time, zone) //		
					.withNano(0) //
					.atZone(zone) //
					.toInstant();
		} else if (level == 2) {
			return LocalDateTime.ofInstant(time, zone) //					
					.withSecond(0) //
					.withNano(0) //
					.atZone(zone) //
					.toInstant();
		} else if (level == 3) {
			return LocalDateTime.ofInstant(time, zone) //					
					.withMinute(0) //
					.withSecond(0) //
					.withNano(0) //
					.atZone(zone) //
					.toInstant();
		} else if (level == 4) {
			return LocalDateTime.ofInstant(time, zone) //										
					.withHour(0) //
					.withMinute(0) //
					.withSecond(0) //
					.withNano(0) //
					.atZone(zone) //
					.toInstant();
		} else {
			return LocalDateTime.ofInstant(time, zone) //
					.withHour(0) //
					.withMinute(0) //
					.withSecond(0) //
					.withNano(0) //
					.atZone(zone) //
					.toInstant();
		}

	}

	/**
	 * It reset time to max time 
	 * @param instant The Source Instant
	 * @param level The Level to make max <br>
	 * Level 1: nano <br>
	 * Level 2: nano, second <br>
	 * Level 3: nano, second, minute <br>
	 * Level 4: nano, second, minute, hour <br>
	 * @return Instant
	 */
	public static Instant resetTimeToMax(Instant time, int level) {

		LogicUtils.assertNotEmpty(Zone.SYS, "zoneId");
		ZoneId zone = ZoneId.of(Zone.SYS);

		if (level == 1) {
			return LocalDateTime.ofInstant(time, zone) //		
					.withNano(0) //
					.atZone(zone) //
					.toInstant();
		} else if (level == 2) {
			return LocalDateTime.ofInstant(time, zone) //					
					.withSecond(59) //
					.withNano(0) //
					.atZone(zone) //
					.toInstant();
		} else if (level == 3) {
			return LocalDateTime.ofInstant(time, zone) //					
					.withMinute(59) //
					.withSecond(59) //
					.withNano(0) //
					.atZone(zone) //
					.toInstant();
		} else if (level == 4) {
			return LocalDateTime.ofInstant(time, zone) //										
					.withHour(23) //
					.withMinute(59) //
					.withSecond(59) //
					.withNano(0) //
					.atZone(zone) //
					.toInstant();
		} else {
			return LocalDateTime.ofInstant(time, zone) //
					.withHour(23) //
					.withMinute(59) //
					.withSecond(59) //
					.withNano(0) //
					.atZone(zone) //
					.toInstant();
		}

	}

}
