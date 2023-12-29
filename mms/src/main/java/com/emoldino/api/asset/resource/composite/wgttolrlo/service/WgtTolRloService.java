package com.emoldino.api.asset.resource.composite.wgttolrlo.service;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.asset.resource.composite.wgttolrlo.dto.WgtTolRloGetOut;
import com.emoldino.api.asset.resource.composite.wgttolrlo.dto.WgtTolRolGetIn;
import com.emoldino.api.asset.resource.composite.wgttolrlo.repository.WgtTolRolRepository;
import com.emoldino.framework.util.MessageUtils;
import com.emoldino.framework.util.ValueUtils;

import saleson.common.enumeration.MoldLocationStatus;

@Service
@Transactional
public class WgtTolRloService {

	@Autowired
	private WgtTolRolRepository repo;

	public WgtTolRloGetOut get() {
		double pendingCount = repo.count(WgtTolRolGetIn.builder().moldLocationStatus(Arrays.asList(MoldLocationStatus.PENDING)).build());
		double apprCount = repo.count(WgtTolRolGetIn.builder().moldLocationStatus(Arrays.asList(MoldLocationStatus.APPROVED)).build());
		double disapprCount = repo.count(WgtTolRolGetIn.builder().moldLocationStatus(Arrays.asList(MoldLocationStatus.DISAPPROVED, MoldLocationStatus.UNAPPROVED)).build());
		double total = pendingCount + apprCount + disapprCount;
		double pendingRate = pendingCount == 0 || total == 0 ? 0 : ValueUtils.toDouble(100d * pendingCount / total, 0d);
		double disapprRate = disapprCount == 0 || total == 0 ? 0 : ValueUtils.toDouble(100d * disapprCount / total, 0d);
		double apprRate = total == 0d ? 0d : (100d - pendingRate - disapprRate);

		return WgtTolRloGetOut.builder()//
				.title(MessageUtils.get("approved", "Approved", null))//
				.value(ValueUtils.toBigDecimal(apprRate, 1, 0d))//
				.unit("%")//
				.title1(MessageUtils.get("approved", "Approved", null))//
				.value1(ValueUtils.toBigDecimal(apprRate, 1, 0d))//
				.detail1(toDetail(apprCount, total, apprRate))//
				.title2(MessageUtils.get("pending_approval", "Pending Approval", null))//
				.value2(ValueUtils.toBigDecimal(pendingRate, 1, 0d))//
				.detail2(toDetail(pendingCount, total, pendingRate))//
				.title3(MessageUtils.get("disapproved", "Disapproved", null))//
				.value3(ValueUtils.toBigDecimal(disapprRate, 1, 0d))//
				.detail3(toDetail(disapprCount, total, disapprRate))//
				.build();
	}

	private String toDetail(double value, double total, double rate) {
		return ValueUtils.toLong(value, 0L) + "/" + ValueUtils.toLong(total, 0L) + " (" + ValueUtils.toBigDecimal(rate, 1, 0d) + "%)";
	}

}
