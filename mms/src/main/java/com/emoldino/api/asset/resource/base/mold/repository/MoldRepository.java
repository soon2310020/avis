package com.emoldino.api.asset.resource.base.mold.repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.asset.resource.base.mold.dto.ToolingStatusSummary;
import com.emoldino.api.asset.resource.base.mold.dto.ToolingStatusSummaryGetIn;
import com.emoldino.api.asset.resource.base.mold.dto.ToolingUtilizationSummary;
import com.emoldino.api.asset.resource.base.mold.dto.ToolingUtilizationSummaryGetIn;
import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingUtilizationStatus;
import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.common.resource.base.option.dto.UtilizationConfig;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository("moldRepository2")
public class MoldRepository {

	public ToolingStatusSummary findAllStatusSummary(ToolingStatusSummaryGetIn input) {
		JPQLQuery<Tuple> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.mold.toolingStatus, Q.mold.count())//
				.from(Q.mold)//
				.groupBy(Q.mold.toolingStatus);

		Set<EntityPathBase<?>> join = new HashSet<>();

		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

		if (!ObjectUtils.isEmpty(input.getUtilizationStatus())) {
			UtilizationConfig config = MoldUtils.getUtilizationConfig();
			BooleanBuilder filter = new BooleanBuilder();
			if (ToolingUtilizationStatus.LOW.equals(input.getUtilizationStatus())) {
				QueryUtils.gtAndLoe(filter, Q.mold.utilizationRate, null, config.getLow());
			} else if (ToolingUtilizationStatus.MEDIUM.equals(input.getUtilizationStatus())) {
				QueryUtils.gtAndLoe(filter, Q.mold.utilizationRate, config.getLow(), config.getMedium());
			} else if (ToolingUtilizationStatus.HIGH.equals(input.getUtilizationStatus())) {
				QueryUtils.gtAndLoe(filter, Q.mold.utilizationRate, config.getMedium(), config.getHigh());
			} else {
				QueryUtils.gtAndLoe(filter, Q.mold.utilizationRate, config.getHigh(), null);
			}
			query.where(filter);
		}

		ToolingStatusSummary summary = new ToolingStatusSummary();
		query.fetch().forEach(tuple -> {
			ToolingStatus status = tuple.get(0, ToolingStatus.class);
			Long count = tuple.get(1, Long.class);
			if (ToolingStatus.IN_PRODUCTION.equals(status)) {
				summary.setInProduction(count);
			} else if (ToolingStatus.IDLE.equals(status)) {
				summary.setIdle(count);
			} else if (ToolingStatus.INACTIVE.equals(status)) {
				summary.setInactive(count);
			} else if (ToolingStatus.SENSOR_DETACHED.equals(status)) {
				summary.setSensorDetached(count);
			} else if (ToolingStatus.SENSOR_OFFLINE.equals(status)) {
				summary.setSensorOffline(count);
			} else if (ToolingStatus.ON_STANDBY.equals(status)) {
				summary.setOnStandby(count);
			} else if (ToolingStatus.NO_SENSOR.equals(status)) {
				summary.setNoSensor(count);
//			} else if (ToolingStatus.DISABLED.equals(status)) {
//				summary.setDisabled(count);
//			} else if (ToolingStatus.UNKNOWN.equals(status)) {
//				summary.setUnknown(count);
			}
		});
		return summary;
	}

	public ToolingUtilizationSummary findAllUtilizationSummary(ToolingUtilizationSummaryGetIn input) {
		UtilizationConfig config = MoldUtils.getUtilizationConfig();
		ToolingUtilizationSummary summary = new ToolingUtilizationSummary();
		{
			JPQLQuery<Long> query = toUtilizationStatusQuery(input, null, config.getLow());
			summary.setLow(query.fetchFirst());
		}
		{
			JPQLQuery<Long> query = toUtilizationStatusQuery(input, config.getLow(), config.getMedium());
			summary.setMedium(query.fetchFirst());
		}
		{
			JPQLQuery<Long> query = toUtilizationStatusQuery(input, config.getMedium(), config.getHigh());
			summary.setHigh(query.fetchFirst());
		}
		{
			JPQLQuery<Long> query = toUtilizationStatusQuery(input, config.getHigh(), null);
			summary.setProlonged(query.fetchFirst());
		}
		return summary;
	}

	private JPQLQuery<Long> toUtilizationStatusQuery(ToolingUtilizationSummaryGetIn input, Integer min, Integer max) {
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.mold.count())//
				.from(Q.mold);

		Set<EntityPathBase<?>> join = new HashSet<>();

		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

		BooleanBuilder filter = new BooleanBuilder();
		QueryUtils.gtAndLoe(filter, Q.mold.utilizationRate, min, max);
		query.where(filter);

		return query;
	}

	public double findWeeklyAvgShotCount(Long moldId, List<String> weeks) {
		LogicUtils.assertNotNull(moldId, "moldId");
		LogicUtils.assertNotEmpty(weeks, "weeks");

		String year = null;
		for (String week : weeks) {
			String weekYear = week.substring(0, 4);
			year = year == null || year.compareTo(weekYear) > 0 ? weekYear : year;
		}

		JPQLQuery<Float> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.statistics.shotCount.sum().floatValue()//
						.divide(Q.statistics.week.countDistinct())//
						.coalesce(0f))//
				.from(Q.statistics)//
				.where(Q.statistics.moldId.eq(moldId)//
						.and(Q.statistics.year.goe(year))//
						.and(Q.statistics.week.in(weeks))//
						.and(Q.statistics.shotCount.gt(0)));
		return ValueUtils.toDouble(query.fetchOne(), 0d);
	}

}
