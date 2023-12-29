package com.emoldino.api.asset.resource.composite.wgttolina.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.asset.resource.composite.wgttolina.dto.WgtTolInaGetIn;
import com.emoldino.api.asset.resource.composite.wgttolina.dto.WgtTolInaGetOut;
import com.emoldino.api.asset.resource.composite.wgttolina.repository.WgtTolInaRepository;
import com.emoldino.api.common.resource.base.option.dto.InactiveConfig;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.MessageUtils;

@Service
@Transactional
public class WgtTolInaService {

	public WgtTolInaGetOut get(WgtTolInaGetIn input) {
		WgtTolInaGetOut output = new WgtTolInaGetOut();
		InactiveConfig config = MoldUtils.getInactiveConfig();
		output.setConfig(config);

		Instant level1 = minusMonths(config.getLevel1());
		output.setLevel1(DateUtils2.format(level1, DatePattern.yyyyMMddHHmmss, Zone.GMT));
		Instant level2 = minusMonths(config.getLevel2());
		output.setLevel2(DateUtils2.format(level2, DatePattern.yyyyMMddHHmmss, Zone.GMT));
		Instant level3 = minusMonths(config.getLevel3());
		output.setLevel3(DateUtils2.format(level3, DatePattern.yyyyMMddHHmmss, Zone.GMT));

		if (level1 != null && level2 != null) {
			long count = BeanUtils.get(WgtTolInaRepository.class).countByGtAndLoe(input, level2, level1);
			output.setTitle1(toTitle(config.getLevel1()));
			output.setValue1(count);
		}
		if (level2 != null && level3 != null) {
			long count = BeanUtils.get(WgtTolInaRepository.class).countByGtAndLoe(input, level3, level2);
			output.setTitle2(toTitle(config.getLevel2()));
			output.setValue2(count);
		}
		if (level3 != null) {
			long count = BeanUtils.get(WgtTolInaRepository.class).countByGtAndLoe(input, null, level3);
			output.setTitle3(toTitle(config.getLevel3()));
			output.setValue3(count);
		}

		return output;
	}

	private String toTitle(Long level) {
		return "> " + level + " " + MessageUtils.get("months", null);
	}

	private Instant minusMonths(Long value) {
		if (value == null) {
			return null;
		}
		return DateUtils2.plusMonths(DateUtils2.getInstant(), -value.intValue(), Zone.GMT);
	}

}
