package com.emoldino.api.asset.resource.composite.toleol.repository;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.asset.resource.composite.toleol.dto.QTolEolItem;
import com.emoldino.api.asset.resource.composite.toleol.dto.TolEolGetIn;
import com.emoldino.api.asset.resource.composite.toleol.dto.TolEolItem;
import com.emoldino.api.common.resource.base.option.dto.RefurbPriorityConfig;
import com.emoldino.api.common.resource.base.option.enumeration.RefurbPriorityCheckBy;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.common.enumeration.PriorityType;
import saleson.model.QMoldEndLifeCycle;

@Repository
public class TolEolRepository {
	private static final long DAY = 1000 * 60 * 60 * 24;
	private static final List<TimeScale> TIME_SCALE_SUPPORTED = Arrays.asList(TimeScale.HALF);

	public long count(TolEolGetIn input) {
		ValueUtils.assertTimeSetting(input, TIME_SCALE_SUPPORTED);

		QMoldEndLifeCycle qEndLife = QMoldEndLifeCycle.moldEndLifeCycle;
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.mold.count())//
				.from(Q.mold)//
				.leftJoin(qEndLife).on(qEndLife.moldId.eq(Q.mold.id));

		Set<EntityPathBase<?>> join = new HashSet<>();

		applyFilter(query, join, input);

		return query.fetchFirst();
	}

	public Page<TolEolItem> findAll(TolEolGetIn input, Pageable pageable) {
		QMoldEndLifeCycle qEndLife = QMoldEndLifeCycle.moldEndLifeCycle;

		RefurbPriorityConfig config = MoldUtils.getRefurbPriorityConfig();
		NumberExpression<Float> remainingDays = qEndLife.remainingDays.floatValue().divide(30f);
		PriorityType defaultPriority = PriorityType.LOW;

		StringExpression refurbPriority;
		if (RefurbPriorityCheckBy.UTILIZATION_RATE.equals(config.getCheckBy())) {//
			refurbPriority = new CaseBuilder()//
					.when(Q.mold.utilizationRate.gt(config.getLow()).and(Q.mold.utilizationRate.loe(config.getMedium())))//
					.then(PriorityType.MEDIUM.name())//
					.when(Q.mold.utilizationRate.gt(config.getMedium()))//
					.then(PriorityType.HIGH.name())//
					.otherwise(defaultPriority.name());
		} else {
			refurbPriority = new CaseBuilder()//
					.when(remainingDays.gt(config.getMedium()).and(remainingDays.loe(config.getLow())))//
					.then(PriorityType.MEDIUM.name())//
					.when(remainingDays.loe(config.getMedium()))//
					.then(PriorityType.HIGH.name())//
					.otherwise(defaultPriority.name());
		}

		JPQLQuery<TolEolItem> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(new QTolEolItem(//
						Q.mold.id, //
						Q.mold.equipmentCode, //
						Q.mold.toolingStatus, //
						Q.part.name.min().as("firstPartName"), //
						Q.mold.lastShot.as("accumShotCount"), //
						Q.mold.designedShot.as("designedShotCount"), //
						Q.mold.utilizationRate, //
						qEndLife.remainingDays.min().coalesce(36500).as("remainingDays"), //
						Q.location.timeZoneId, //
						refurbPriority.max().as("refurbPriority"), //
						Q.mold.cost, //
						Q.mold.salvageValue//
				))//
				.from(Q.mold)//
				.leftJoin(Q.moldPart).on(Q.moldPart.moldId.eq(Q.mold.id))//
				.leftJoin(Q.part).on(Q.part.id.eq(Q.moldPart.partId).and(QueryUtils.isPart()))//
				.leftJoin(qEndLife).on(qEndLife.moldId.eq(Q.mold.id))//
				.leftJoin(Q.location).on(Q.location.id.eq(Q.mold.locationId))//
				.leftJoin(Q.area).on(Q.area.id.eq(Q.mold.areaId))//
				.groupBy(Q.mold.id);

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, input);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("moldCode", Q.mold.equipmentCode)//
				.put("toolingStatus", Q.mold.toolingStatus)//
				.put("parts", Expressions.stringPath("firstPartName"))//
				.put("utilizationRate", Q.mold.utilizationRate)//
				.put("utilizationStatus", Q.mold.utilizationRate)//
				.put("remainingDays", Expressions.numberPath(Integer.class, "remainingDays"))//
				.put("eolDate", Expressions.numberPath(Integer.class, "remainingDays"))//
				.put("refurbPriority", getRefurbPriority(refurbPriority))//
				.put("cost", Q.mold.cost)//
				.put("salvageValue", Q.mold.salvageValue)//
				.build();

		QueryUtils.applyPagination(query, pageable, fieldMap, Q.mold.equipmentCode.asc());
		QueryResults<TolEolItem> results = query.fetchResults();
		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	@Deprecated
	private static NumberExpression<Integer> getRefurbPriority() {
		QMoldEndLifeCycle qEndLife = QMoldEndLifeCycle.moldEndLifeCycle;
		NumberExpression<Integer> expression = new CaseBuilder()//
				.when(qEndLife.priority.stringValue().max().isNull())//
				.then(1)//
				.when(qEndLife.priority.stringValue().max().eq("LOW"))//
				.then(2)//
				.when(qEndLife.priority.stringValue().max().eq("MEDIUM"))//
				.then(3)//
				.otherwise(4);
		return expression;
	}

	private static NumberExpression<Integer> getRefurbPriority(StringExpression refurbPriority) {
		NumberExpression<Integer> expression = new CaseBuilder()//
				.when(refurbPriority.max().isNull())//
				.then(1)//
				.when(refurbPriority.max().eq(PriorityType.LOW.name()))//
				.then(2)//
				.when(refurbPriority.max().eq(PriorityType.MEDIUM.name()))//
				.then(3)//
				.otherwise(4);
		return expression;
	}

	private void applyFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, TolEolGetIn input) {
		RefurbPriorityConfig config = MoldUtils.getRefurbPriorityConfig();
		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());
		BooleanBuilder filter = new BooleanBuilder();
		QMoldEndLifeCycle qEndLife = QMoldEndLifeCycle.moldEndLifeCycle;

		filter.and(Q.mold.toolingStatus.notIn(ToolingStatus.NO_SENSOR, ToolingStatus.ON_STANDBY));

		PriorityType inputRefurbPriority = input.getRefurbPriority();
		if (inputRefurbPriority != null) {
			NumberExpression<Float> remainingDays = qEndLife.remainingDays.floatValue();

			if (RefurbPriorityCheckBy.UTILIZATION_RATE.equals(config.getCheckBy())) {
				if (PriorityType.MEDIUM.equals(inputRefurbPriority)) {
					filter.and(Q.mold.utilizationRate.gt(config.getLow()).and(Q.mold.utilizationRate.loe(config.getMedium())));
				} else if (PriorityType.HIGH.equals(inputRefurbPriority)) {
					filter.and(Q.mold.utilizationRate.gt(config.getMedium()));
				}
			} else {
				NumberExpression<Float> remainingDaysInMonths = remainingDays.divide(30f);
				if (PriorityType.MEDIUM.equals(inputRefurbPriority)) {
					filter.and(remainingDaysInMonths.gt(config.getMedium()).and(remainingDaysInMonths.loe(config.getLow())));
				} else if (PriorityType.HIGH.equals(inputRefurbPriority)) {
					filter.and(remainingDaysInMonths.loe(config.getMedium()));
				}
			}
		}

		if (!ObjectUtils.isEmpty(input.getTimeValue())) {
			ValueUtils.assertTimeSetting(input, TIME_SCALE_SUPPORTED);
			Pair<String, String> dateRange = DateUtils2.toDateRange(input);
			Instant now = DateUtils2.toInstant(DateUtils2.getString(DatePattern.yyyyMMdd, Zone.GMT), DatePattern.yyyyMMdd, Zone.GMT);
			Instant from = DateUtils2.toInstant(dateRange.getFirst(), DatePattern.yyyyMMdd, Zone.GMT);
			Instant to = DateUtils2.toInstant(dateRange.getSecond(), DatePattern.yyyyMMdd, Zone.GMT);
			long fromDays = (from.toEpochMilli() - now.toEpochMilli()) / DAY;
			long toDays = (to.toEpochMilli() - now.toEpochMilli()) / DAY;
			if (fromDays > 0L) {
				filter.and(qEndLife.remainingDays.goe(fromDays));
			}
			filter.and(qEndLife.remainingDays.loe(toDays));
		}
		query.where(filter);
	}

}
