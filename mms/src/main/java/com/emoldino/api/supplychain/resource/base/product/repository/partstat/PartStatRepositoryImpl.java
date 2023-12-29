package com.emoldino.api.supplychain.resource.base.product.repository.partstat;

import java.util.List;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.supplychain.resource.base.product.util.ProductUtils;
import com.emoldino.framework.util.LogicUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;

public class PartStatRepositoryImpl extends QuerydslRepositorySupport implements PartStatRepositoryCustom {
	public PartStatRepositoryImpl() {
		super(PartStat.class);
	}

	@Override
	public List<PartStat> findAllWeeklyByProduct(String week, Long productId, Long partId, List<Long> supplierId, Long moldId) {
		JPQLQuery<PartStat> query = toQueryWeeklyByProduct(week, productId, partId, supplierId, moldId, false);
		return query.fetch();
	}

	@Override
	public List<PartStat> findAllWeeklyByBrand(String week, Long brandId, Long partId, List<Long> supplierId, Long moldId) {
		JPQLQuery<PartStat> query = toQueryWeeklyByProduct(week, brandId, partId, supplierId, moldId, true);
		return query.fetch();
	}

	private JPQLQuery<PartStat> toQueryWeeklyByProduct(String week, Long productId, Long partId, List<Long> supplierId, Long moldId, boolean brand) {
		LogicUtils.assertNotEmpty(week, "week");
		if (brand) {
			LogicUtils.assertNotNull(productId, "brandId");
		}

		QPartStat table = QPartStat.partStat;
		BooleanBuilder filter = new BooleanBuilder();
		if (brand) {
			filter.and(table.productId.in(ProductUtils.filterProductByBrand(productId)));
		} else if (productId != null) {
			filter.and(table.productId.eq(productId));
		}
		filter.and(table.week.eq(week));
		if (partId != null) {
			filter.and(table.partId.eq(partId));
		}
		if (!ObjectUtils.isEmpty(supplierId)) {
			filter.and(table.supplierId.in(supplierId));
		}
		if (moldId != null) {
			filter.and(table.moldId.eq(moldId));
		}

		JPQLQuery<PartStat> query = from(table)//
				.where(filter)//
				.groupBy(table.productId, table.partId, table.supplierId, table.locationId, table.moldId, table.day)//
				.select(Projections.constructor(PartStat.class, //
						table.productId, //
						table.partId, //
						table.supplierId, //
						table.locationId, //
						table.moldId, //
						table.week.max().as("week"), //
						table.year.max().as("year"), //
						table.month.max().as("month"), //
						table.day, //
						table.produced.sum().as("produced"), //
						table.producedVal.sum().as("producedVal"), //
						table.dailyCapacity.sum().as("dailyCapacity")//
				));
		return query;
	}
}
