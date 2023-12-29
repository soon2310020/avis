package com.emoldino.api.integration.resource.composite.inftol.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.integration.resource.composite.inftol.dto.InfTolDailySummaryIn;
import com.emoldino.api.integration.resource.composite.inftol.dto.InfTolGetIn;
import com.emoldino.api.integration.resource.composite.inftol.dto.InfTolItem;
import com.emoldino.api.integration.resource.composite.inftol.dto.InfTolPostItem;
import com.emoldino.api.integration.resource.composite.inftol.dto.InfTolSummaryItem;
import com.emoldino.api.integration.resource.composite.inftol.repository.InfTolRepository;
import com.emoldino.api.integration.util.IntegrationUtils;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ValueUtils;

import saleson.api.mold.MoldRepository;
import saleson.common.enumeration.EquipmentStatus;
import saleson.model.Mold;

@Service
@Transactional
public class InfTolService {

	public Page<InfTolItem> get(InfTolGetIn input, Pageable pageable) {
		IntegrationUtils.checkAuth();

		Page<InfTolItem> page = BeanUtils.get(InfTolRepository.class).findAll(input, pageable);
		return page;
	}

	public InfTolItem get(String toolingId) {
		IntegrationUtils.checkAuth();

		Page<InfTolItem> page = BeanUtils.get(InfTolRepository.class).findAll(//
				InfTolGetIn.builder().toolingId(toolingId).build(), //
				PageRequest.of(0, 1)//
		);
		return page.get().findFirst().orElse(new InfTolItem());
	}

	public void post(String toolingId, InfTolPostItem tooling) {
		IntegrationUtils.checkAuth();

		Mold mold = BeanUtils.get(MoldRepository.class).findByEquipmentCode(toolingId);
		if (mold == null) {
			mold = new Mold();
			mold.setEquipmentCode(toolingId);
			mold.setEquipmentStatus(EquipmentStatus.AVAILABLE);
			mold.setToolingStatus(ToolingStatus.NO_SENSOR);
		}

		mold.setToolingType(tooling.getToolingType());
		mold.setToolDescription(tooling.getToolDescription());
		if (tooling.getToolSizeWidth() != null && tooling.getToolSizeWidth() > 0L//
				&& tooling.getToolSizeLength() != null && tooling.getToolSizeLength() > 0L //
				&& tooling.getToolSizeHeight() != null && tooling.getToolSizeHeight() > 0L) {
			mold.setSize(tooling.getToolSizeWidth() + "x" + tooling.getToolSizeLength() + "x" + tooling.getToolSizeHeight());
		}
		mold.setSizeUnit(tooling.getToolSizeUnit());
		mold.setWeight(tooling.getToolWeight() == null ? null : tooling.getToolWeight() + "");
		mold.setWeightUnit(tooling.getToolWeightUnit());

		mold.setDesignedShot(ValueUtils.toInteger(tooling.getWarrantyShotCount(), 0));

		BeanUtils.get(MoldRepository.class).save(mold);
	}

	public Page<InfTolSummaryItem> getDailySummary(InfTolDailySummaryIn input, Pageable pageable) {
		IntegrationUtils.checkAuth();

		ValueUtils.assertNotEmpty(input.getDate(), "date");

		Page<InfTolSummaryItem> output = BeanUtils.get(InfTolRepository.class).findDailySummary(input, pageable);
		return output;
	}

}
