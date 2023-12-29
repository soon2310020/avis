package com.emoldino.api.asset.resource.composite.toladt.repository;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingUtilizationStatus;
import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.asset.resource.composite.toladt.dto.QTolAdtItem;
import com.emoldino.api.asset.resource.composite.toladt.dto.TolAdtGetIn;
import com.emoldino.api.asset.resource.composite.toladt.dto.TolAdtGetOut.TolAdtAreaSummary;
import com.emoldino.api.asset.resource.composite.toladt.dto.TolAdtGetOut.TolAdtDistribution;
import com.emoldino.api.asset.resource.composite.toladt.dto.TolAdtGetOut.TolAdtStatusSummary;
import com.emoldino.api.asset.resource.composite.toladt.dto.TolAdtGetOut.TolAdtUtilizationSummary;
import com.emoldino.api.asset.resource.composite.toladt.dto.TolAdtItem;
import com.emoldino.api.asset.resource.composite.toladt.dto.TolAdtRelocationHistoriesGetIn;
import com.emoldino.api.asset.resource.composite.toladt.dto.TolAdtRelocationHistoriesItem;
import com.emoldino.api.asset.resource.composite.toladt.enumeration.TolAdtAreaType;
import com.emoldino.api.common.resource.base.location.enumeration.AreaType;
import com.emoldino.api.common.resource.base.option.dto.UtilizationConfig;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class TolAdtRepository {

	public Page<TolAdtItem> findAll(TolAdtGetIn input, Pageable pageable) {
		JPQLQuery<TolAdtItem> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(new QTolAdtItem(//
						Q.mold.id, //
						Q.mold.equipmentCode, //
						Q.mold.toolingStatus, //
						Q.supplier.id, //
						Q.supplier.name, //
						Q.supplier.companyCode, //
						Q.company.id, //
						Q.company.name, //
						Q.company.companyCode, //
						Q.location.id, //
						Q.location.name, //
						Q.location.locationCode, //
						Q.location.timeZoneId, //
						Q.area.id, //
						Q.area.name, //
						Q.mold.utilizationRate, //
						Q.moldLocation.notificationAt.max().as("relocatedAt")//
				))//
				.from(Q.mold)//
				.leftJoin(Q.supplier).on(Q.supplier.id.eq(Q.mold.supplierCompanyId))//
				.leftJoin(Q.company).on(Q.company.id.eq(Q.mold.companyId))//
				.leftJoin(Q.location).on(Q.location.id.eq(Q.mold.locationId))//
				.leftJoin(Q.area).on(Q.area.id.eq(Q.mold.areaId))//
				.leftJoin(Q.moldLocation).on(Q.moldLocation.moldId.eq(Q.mold.id))//
				.groupBy(Q.mold.id);

		Set<EntityPathBase<?>> join = new HashSet<>();

		applyFilter(query, join, input);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("moldCode", Q.mold.equipmentCode)//
				.put("supplierName", Q.supplier.name)//
				.put("locationName", Q.location.name)//
				.put("areaName", Q.area.name)//
				.put("toolingStatus", Q.mold.toolingStatus)//
				.put("utilizationRate", Q.mold.utilizationRate)//
				.put("relocatedDateTime", Expressions.datePath(Instant.class, "relocatedAt"))//
				.build();
		QueryUtils.applyPagination(query, pageable, fieldMap, Q.mold.equipmentCode.asc());
		QueryResults<TolAdtItem> results = query.fetchResults();
		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	public List<TolAdtDistribution> findAllDistributions(TolAdtGetIn input) {
		JPQLQuery<TolAdtDistribution> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(//
						TolAdtDistribution.class, //
						Q.location.id, //
						Q.location.name, //
						Q.location.latitude, //
						Q.location.longitude, //
						Q.location.address//
				))//
				.distinct()//
				.from(Q.mold);

		Set<EntityPathBase<?>> join = new HashSet<>();
		QueryUtils.joinLocationByMold(query, join);

		applyFilter(query, join, input);

		List<TolAdtDistribution> content = query.fetch();
		return content;
	}

	public TolAdtUtilizationSummary findAllUtilizationSummary(TolAdtGetIn input) {
		boolean empty = ObjectUtils.isEmpty(input.getUtilizationStatus());
		UtilizationConfig config = MoldUtils.getUtilizationConfig();
		TolAdtUtilizationSummary summary = new TolAdtUtilizationSummary();
		if (empty || ToolingUtilizationStatus.LOW.equals(input.getUtilizationStatus())) {
			JPQLQuery<Long> query = toUtilizationCountQuery(input, null, config.getLow());
			summary.setLow(query.fetchFirst());
		}
		if (empty || ToolingUtilizationStatus.MEDIUM.equals(input.getUtilizationStatus())) {
			JPQLQuery<Long> query = toUtilizationCountQuery(input, config.getLow(), config.getMedium());
			summary.setMedium(query.fetchFirst());
		}
		if (empty || ToolingUtilizationStatus.HIGH.equals(input.getUtilizationStatus())) {
			JPQLQuery<Long> query = toUtilizationCountQuery(input, config.getMedium(), config.getHigh());
			summary.setHigh(query.fetchFirst());
		}
		if (empty || ToolingUtilizationStatus.PROLONGED.equals(input.getUtilizationStatus())) {
			JPQLQuery<Long> query = toUtilizationCountQuery(input, config.getHigh(), null);
			summary.setProlonged(query.fetchFirst());
		}
		return summary;
	}

	private JPQLQuery<Long> toUtilizationCountQuery(TolAdtGetIn input, Integer min, Integer max) {
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.mold.count())//
				.from(Q.mold);

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, input);

		BooleanBuilder filter = new BooleanBuilder();
		QueryUtils.gtAndLoe(filter, Q.mold.utilizationRate, min, max);
		query.where(filter);

		return query;
	}

	public TolAdtStatusSummary findAllStatusSummary(TolAdtGetIn input) {
		JPQLQuery<Tuple> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.mold.toolingStatus, Q.mold.count())//
				.from(Q.mold)//
				.groupBy(Q.mold.toolingStatus);

		Set<EntityPathBase<?>> join = new HashSet<>();

		applyFilter(query, join, input);

		TolAdtStatusSummary summary = new TolAdtStatusSummary();
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

	public TolAdtAreaSummary findAllAreaSummary(TolAdtGetIn input) {
		JPQLQuery<Tuple> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.area.areaType, Q.mold.count())//
				.from(Q.mold)//
				.groupBy(Q.area.areaType);

		Set<EntityPathBase<?>> join = new HashSet<>();

		applyFilter(query, join, input);

		QueryUtils.leftJoinLocationByMold(query, join);
		QueryUtils.leftJoin(query, join, Q.area, () -> Q.area.id.eq(Q.mold.areaId));

		TolAdtAreaSummary summary = new TolAdtAreaSummary();
		query.fetch().forEach(tuple -> {
			AreaType areaType = tuple.get(0, AreaType.class);
			Long count = tuple.get(1, Long.class);
			if (AreaType.PRODUCTION_AREA.equals(areaType)) {
				summary.setProduction(count);
			} else if (AreaType.MAINTENANCE_AREA.equals(areaType)) {
				summary.setMaintenance(count);
			} else if (AreaType.WAREHOUSE.equals(areaType)) {
				summary.setWarehouse(count);
			} else {
				summary.setUnknown(count);
			}
		});
		return summary;
	}

	private void applyFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, TolAdtGetIn input) {
		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());
		BooleanBuilder filter = new BooleanBuilder();
		QueryUtils.eq(filter, Q.mold.locationId, input.getLocationId());
		if (input.getUtilizationStatus() != null) {
			UtilizationConfig config = MoldUtils.getUtilizationConfig();
			if (ToolingUtilizationStatus.LOW.equals(input.getUtilizationStatus())) {
				QueryUtils.gtAndLoe(filter, Q.mold.utilizationRate, null, config.getLow());
			} else if (ToolingUtilizationStatus.MEDIUM.equals(input.getUtilizationStatus())) {
				QueryUtils.gtAndLoe(filter, Q.mold.utilizationRate, config.getLow(), config.getMedium());
			} else if (ToolingUtilizationStatus.HIGH.equals(input.getUtilizationStatus())) {
				QueryUtils.gtAndLoe(filter, Q.mold.utilizationRate, config.getMedium(), config.getHigh());
			} else if (ToolingUtilizationStatus.PROLONGED.equals(input.getUtilizationStatus())) {
				QueryUtils.gtAndLoe(filter, Q.mold.utilizationRate, config.getHigh(), null);
			}
		}
		QueryUtils.eq(filter, Q.mold.toolingStatus, input.getToolingStatus());
		if (TolAdtAreaType.UNKNOWN.equals(input.getAreaType())) {
			filter.and(Q.mold.areaId.isNull());
		} else if (input.getAreaType() != null) {
			filter.and(Q.mold.area.areaType.eq(AreaType.valueOf(input.getAreaType().name())));
		}
		if (!ObjectUtils.isEmpty(input.getQuery())) {
			QueryUtils.leftJoin(query, join, Q.location, () -> Q.location.id.eq(Q.mold.locationId));
			QueryUtils.leftJoin(query, join, Q.area, () -> Q.area.id.eq(Q.mold.areaId));
			QueryUtils.leftJoin(query, join, Q.counter, () -> Q.counter.id.eq(Q.mold.counterId));
			String searchWord = input.getQuery();
			filter.and(//
					Q.mold.equipmentCode.containsIgnoreCase(searchWord)//
							.or(Q.location.name.containsIgnoreCase(searchWord))//
							.or(Q.location.locationCode.containsIgnoreCase(searchWord))//
							.or(Q.area.name.containsIgnoreCase(searchWord))//
							.or(Q.counter.equipmentCode.eq(searchWord))//
			);
		}
		query.where(filter);
	}

	public Page<TolAdtRelocationHistoriesItem> findAllRelocationHistories(Long moldId, TolAdtRelocationHistoriesGetIn input, Pageable pageable) {
		LogicUtils.assertNotNull(moldId, "mold_id");

		JPQLQuery<TolAdtRelocationHistoriesItem> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(TolAdtRelocationHistoriesItem.class, Q.moldLocation))//
				.from(Q.mold)//
				.innerJoin(Q.moldLocation).on(Q.moldLocation.moldId.eq(Q.mold.id));

		Set<EntityPathBase<?>> join = new HashSet<>();
		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

		BooleanBuilder filter = new BooleanBuilder();
		filter.and(Q.mold.id.eq(moldId));
		query.where(filter);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("notificationAt", Q.moldLocation.notificationAt)//
				.put("relocatedAt", Q.moldLocation.notificationAt)//
				.put("relocatedDateTime", Q.moldLocation.notificationAt)//
				.build();
		QueryUtils.applyPagination(query, pageable, fieldMap, Q.moldLocation.notificationAt.desc());

		QueryResults<TolAdtRelocationHistoriesItem> results = query.fetchResults();
		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

}
