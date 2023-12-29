package com.emoldino.api.asset.resource.composite.wgttoltco.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.asset.resource.composite.wgttoltco.dto.WgtTolTcoGetIn;
import com.emoldino.api.asset.resource.composite.wgttoltco.dto.WgtTolTcoGetOut;
import com.emoldino.api.common.resource.base.masterdata.dto.Currency;
import com.emoldino.api.common.resource.base.masterdata.util.MasterDataUtils;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.common.enumeration.ObjectType;
import saleson.model.QWorkOrder;
import saleson.model.QWorkOrderAsset;
import saleson.model.QWorkOrderCost;

@Service
@Transactional
public class WgtTolTcoService {

	public WgtTolTcoGetOut get(WgtTolTcoGetIn input) {
		Currency currency = MasterDataUtils.getMainCurrency();
		double rate = currency.getExchangeRate();
		long aCost = 0L;
		{
			JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
					.select(Q.mold.cost.sum().longValue().coalesce(0L))//
					.from(Q.mold);
			Set<EntityPathBase<?>> join = new HashSet<>();
			QueryUtils.applyMoldFilter(query, join, input.getFilterCode());
			aCost = query.fetchOne();
		}
		long mCost = 0L;
		{
			QWorkOrderAsset qAsset = QWorkOrderAsset.workOrderAsset;
			QWorkOrder qMaint = QWorkOrder.workOrder;
			QWorkOrderCost qCost = QWorkOrderCost.workOrderCost;
			JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
					.select(qCost.cost.sum().longValue().coalesce(0L))//
					.from(Q.mold);
			Set<EntityPathBase<?>> join = new HashSet<>();
			QueryUtils.applyMoldFilter(query, join, input.getFilterCode());
			QueryUtils.join(query, join, qAsset, () -> qAsset.type.eq(ObjectType.TOOLING).and(qAsset.assetId.eq(Q.mold.id)));
			QueryUtils.join(query, join, qMaint, () -> qMaint.id.eq(qAsset.workOrderId));
			QueryUtils.join(query, join, qCost, () -> qCost.workOrderId.eq(qMaint.id).and(qCost.cost.isNotNull()));
			mCost = query.fetchOne();
		}
		return WgtTolTcoGetOut.builder()//
				.acquisitionCost(rate == 0d ? aCost : ValueUtils.toLong(aCost / rate, 0L))//
				.maintenanceCost(mCost)//
				.currencyCode(currency.getCurrencyType().name())//
				.currencySymbol(currency.getCurrencyType().getTitle())//
				.build();
	}

}
