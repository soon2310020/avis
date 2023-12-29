package com.emoldino.api.analysis.resource.composite.wgtcyctimcpl.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.analysis.resource.base.data.repository.cycletimedeviation.QCycleTimeDeviation;
import com.emoldino.api.analysis.resource.composite.wgtcyctimcpl.dto.QWgtCycTimCplGetOut;
import com.emoldino.api.analysis.resource.composite.wgtcyctimcpl.dto.WgtCycTimCplGetIn;
import com.emoldino.api.analysis.resource.composite.wgtcyctimcpl.dto.WgtCycTimCplGetOut;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.MessageUtils;
import com.emoldino.framework.util.QueryUtils;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Service
@Transactional
public class WgtCycTimCplService {

	public WgtCycTimCplGetOut get(WgtCycTimCplGetIn input) {
		QCycleTimeDeviation qCtd = QCycleTimeDeviation.cycleTimeDeviation;

		JPQLQuery<WgtCycTimCplGetOut> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(new QWgtCycTimCplGetOut(//
						qCtd.withinL1ToleranceSc.abs().longValue().sum(), //
						qCtd.withinUpperL2ToleranceSc.abs().longValue().sum().add(qCtd.aboveToleranceSc.abs().longValue().sum()), //
						qCtd.withinLowerL2ToleranceSc.abs().longValue().sum().add(qCtd.belowToleranceSc.abs().longValue().sum())//
				))//
				.from(Q.mold);

		Set<EntityPathBase<?>> join = new HashSet<>();
		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

		QueryUtils.join(query, join, qCtd, () -> qCtd.moldId.eq(Q.mold.id));

		WgtCycTimCplGetOut output = query.fetchOne();
		output.setTitle(MessageUtils.get("compliance", "Compliance", null));
		output.setTitle1(MessageUtils.get("within", "Within", null));
		output.setTitle2(MessageUtils.get("above", "Above", null));
		output.setTitle3(MessageUtils.get("below", "Below", null));
		return output;
	}

}
