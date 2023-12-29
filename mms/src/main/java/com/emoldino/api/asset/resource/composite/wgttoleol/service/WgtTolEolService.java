package com.emoldino.api.asset.resource.composite.wgttoleol.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.asset.resource.composite.wgttoleol.dto.WgtTolEolGetIn;
import com.emoldino.api.asset.resource.composite.wgttoleol.dto.WgtTolEolGetOut;
import com.emoldino.api.common.resource.base.option.dto.RefurbPriorityConfig;
import com.emoldino.api.common.resource.base.option.enumeration.RefurbPriorityCheckBy;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.MessageUtils;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.model.QMoldEndLifeCycle;

@Service
@Transactional
public class WgtTolEolService {

	public WgtTolEolGetOut get(WgtTolEolGetIn input) {
		WgtTolEolGetOut output = new WgtTolEolGetOut();
		RefurbPriorityConfig config = MoldUtils.getRefurbPriorityConfig();
		output.setConfig(config);

		if (RefurbPriorityCheckBy.UTILIZATION_RATE.equals(config.getCheckBy())) {
			output.setTitle1(ValueUtils.toInteger(config.getLow(), 0) + " - " + ValueUtils.toInteger(config.getMedium(), 0) + "% " + MessageUtils.get("utilization", null));
			{
				JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
						.select(Q.mold.countDistinct())//
						.from(Q.mold)//
						.where(Q.mold.utilizationRate.gt(config.getLow()).and(Q.mold.utilizationRate.loe(config.getMedium())));
				Set<EntityPathBase<?>> join = new HashSet<>();
				QueryUtils.applyMoldFilter(query, join, input.getFilterCode());
				output.setValue1(query.fetchOne());
			}
			output.setTitle2(">" + ValueUtils.toInteger(config.getMedium(), 0) + "% " + MessageUtils.get("utilization", null));
			{
				JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
						.select(Q.mold.countDistinct())//
						.from(Q.mold)//
						.where(Q.mold.utilizationRate.gt(config.getMedium()));
				Set<EntityPathBase<?>> join = new HashSet<>();
				QueryUtils.applyMoldFilter(query, join, input.getFilterCode());
				output.setValue2(query.fetchOne());
			}
		} else {
			QMoldEndLifeCycle qEndLife = QMoldEndLifeCycle.moldEndLifeCycle;

			output.setTitle1(ValueUtils.toInteger(config.getMedium(), 0) + " - " + ValueUtils.toInteger(config.getLow(), 0) + " " + MessageUtils.get("months", null));
			{
				JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
						.select(Q.mold.countDistinct())//
						.from(Q.mold)//
						.innerJoin(qEndLife).on(qEndLife.moldId.eq(Q.mold.id))//
						.where(qEndLife.remainingDays.floatValue().divide(30f).gt(config.getMedium()).and(qEndLife.remainingDays.floatValue().divide(30f).loe(config.getLow())));
				Set<EntityPathBase<?>> join = new HashSet<>();
				QueryUtils.applyMoldFilter(query, join, input.getFilterCode());
				output.setValue1(query.fetchOne());
			}
			output.setTitle2("< " + ValueUtils.toInteger(config.getMedium(), 0) + " " + MessageUtils.get("months", null));
			{
				JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
						.select(Q.mold.countDistinct())//
						.from(Q.mold)//
						.innerJoin(qEndLife).on(qEndLife.moldId.eq(Q.mold.id))//
						.where(qEndLife.remainingDays.floatValue().divide(30f).loe(config.getMedium()));
				Set<EntityPathBase<?>> join = new HashSet<>();
				QueryUtils.applyMoldFilter(query, join, input.getFilterCode());
				output.setValue2(query.fetchOne());
			}
		}
		return output;
	}

}
