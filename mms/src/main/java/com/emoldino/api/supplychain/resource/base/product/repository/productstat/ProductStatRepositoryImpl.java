package com.emoldino.api.supplychain.resource.base.product.repository.productstat;

import com.emoldino.api.supplychain.resource.base.product.util.ProductUtils;
import com.querydsl.jpa.JPAExpressions;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import saleson.model.QCategory;

import java.util.List;

public class ProductStatRepositoryImpl extends QuerydslRepositorySupport implements ProductStatRepositoryCustom {
	public ProductStatRepositoryImpl() {
		super(ProductStat.class);
	}

	@Override
	public ProductStat getWeeklyByProduct(String week, Long productId) {
		QProductStat table = QProductStat.productStat;

		BooleanBuilder filter = new BooleanBuilder();
		filter.and(table.productId.eq(productId));
		filter.and(table.week.eq(week));

		JPQLQuery<ProductStat> query = from(table).where(filter)
				.select(Projections.constructor(ProductStat.class, table.productId, table.week, table.year.max().as("year"), table.month.max().as("month"),
						table.day.max().as("day"), table.partCount.max().as("partCount"), table.supplierCount.max().as("supplierCount"), table.moldCount.max().as("moldCount"),
						table.produced.sum().as("produced"), table.producedVal.sum().as("producedVal"), table.dailyCapacity.max().as("dailyCapacity")));
		return query.fetchOne();
	}

	@Override
	public List<ProductStat> getWeeklyByBrand(String week, Long brandId) {
		QProductStat table = QProductStat.productStat;

		BooleanBuilder filter = new BooleanBuilder();
		filter.and(table.productId.in(ProductUtils.filterProductByBrand(brandId)));
		filter.and(table.week.eq(week));

		JPQLQuery<ProductStat> query = from(table).where(filter)
				.select(Projections.constructor(ProductStat.class, table.productId, table.week, table.year.max().as("year"), table.month.max().as("month"),
						table.day.max().as("day"), table.partCount.max().as("partCount"), table.supplierCount.max().as("supplierCount"), table.moldCount.max().as("moldCount"),
						table.produced.sum().as("produced"), table.producedVal.sum().as("producedVal"), table.dailyCapacity.max().as("dailyCapacity")));
		return query.fetch();
	}
}
