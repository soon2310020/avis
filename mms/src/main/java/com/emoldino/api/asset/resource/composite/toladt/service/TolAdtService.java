package com.emoldino.api.asset.resource.composite.toladt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.asset.resource.composite.toladt.dto.TolAdtGetIn;
import com.emoldino.api.asset.resource.composite.toladt.dto.TolAdtGetOut;
import com.emoldino.api.asset.resource.composite.toladt.dto.TolAdtItem;
import com.emoldino.api.asset.resource.composite.toladt.dto.TolAdtRelocationHistoriesGetIn;
import com.emoldino.api.asset.resource.composite.toladt.dto.TolAdtRelocationHistoriesGetOut;
import com.emoldino.api.asset.resource.composite.toladt.dto.TolAdtRelocationHistoriesItem;
import com.emoldino.api.asset.resource.composite.toladt.repository.TolAdtRepository;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ValueUtils;

import saleson.api.mold.MoldRepository;
import saleson.model.Mold;

@Service
@Transactional
public class TolAdtService {
	@Autowired
	private TolAdtRepository repo;

	public TolAdtGetOut get(TolAdtGetIn input, Pageable pageable) {
		Page<TolAdtItem> page = repo.findAll(input, pageable);
		TolAdtGetOut output = new TolAdtGetOut(page.getContent(), page.getPageable(), page.getTotalElements());
		output.setDistributions(repo.findAllDistributions(input));
		output.setUtilizationSummary(repo.findAllUtilizationSummary(input));
		output.setStatusSummary(repo.findAllStatusSummary(input));
		output.setAreaSummary(repo.findAllAreaSummary(input));
		return output;
	}

	public TolAdtRelocationHistoriesGetOut getRelocationHistories(Long moldId, TolAdtRelocationHistoriesGetIn input, Pageable pageable) {
		ValueUtils.assertNotEmpty(moldId, "mold_id");
		Mold mold = BeanUtils.get(MoldRepository.class).findById(moldId).orElse(null);
		Page<TolAdtRelocationHistoriesItem> page = repo.findAllRelocationHistories(moldId, input, pageable);
		TolAdtRelocationHistoriesGetOut output = new TolAdtRelocationHistoriesGetOut(page, moldId, mold == null ? null : mold.getEquipmentCode());
		return output;
	}

}
