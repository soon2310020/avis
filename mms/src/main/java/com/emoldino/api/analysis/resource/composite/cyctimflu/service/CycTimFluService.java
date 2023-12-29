package com.emoldino.api.analysis.resource.composite.cyctimflu.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.emoldino.api.analysis.resource.composite.cyctimflu.dto.CycTimFluDetailsGetIn;
import com.emoldino.api.analysis.resource.composite.cyctimflu.dto.CycTimFluDetailsGetOut;
import com.emoldino.api.analysis.resource.composite.cyctimflu.dto.CycTimFluGetIn;
import com.emoldino.api.analysis.resource.composite.cyctimflu.dto.CycTimFluItem;
import com.emoldino.api.analysis.resource.composite.cyctimflu.dto.CycTimFluPart;
import com.emoldino.api.analysis.resource.composite.cyctimflu.repository.CycTimFluRepository;
import com.emoldino.api.common.resource.composite.flt.dto.FltPart;
import com.emoldino.api.common.resource.composite.flt.dto.FltPartIn;
import com.emoldino.api.common.resource.composite.flt.dto.FltSupplier;
import com.emoldino.api.common.resource.composite.flt.service.FltService;
import com.emoldino.api.common.resource.composite.flt.util.FltUtils;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ValueUtils;

import lombok.extern.slf4j.Slf4j;
import saleson.api.mold.MoldPartRepository;
import saleson.model.MoldPart;

@Slf4j
@Service
public class CycTimFluService {
	private static final List<TimeScale> TIME_SCALE_SUPPORTED = Arrays.asList(TimeScale.WEEK, TimeScale.MONTH, TimeScale.YEAR, TimeScale.CUSTOM);

	@Autowired
	CycTimFluRepository repo;

	public Page<CycTimFluItem> getPage(CycTimFluGetIn input, Pageable pageable) {
		ValueUtils.assertTimeSetting(input, TIME_SCALE_SUPPORTED);

		Page<CycTimFluItem> page = repo.findAll(input, pageable);

		return new PageImpl<>(page.getContent(), pageable, page.getTotalElements());
	}

	public CycTimFluDetailsGetOut getDetailsPage(CycTimFluDetailsGetIn input, Pageable pageable) {
		ValueUtils.assertTimeSetting(input, TIME_SCALE_SUPPORTED);

		FltSupplier supplier = FltUtils.getSupplierById(input.getSupplierId());
		FltPart part = FltUtils.getPartById(input.getPartId());
		Page<CycTimFluDetailsGetOut.CycTimFluDetails> page = repo.findDetailsAll(input, pageable);
		//add part detail
		loadPart(page.getContent());
		return new CycTimFluDetailsGetOut(page.getContent(), pageable, page.getTotalElements(), supplier, part);
	}

	public void loadPart(List<CycTimFluDetailsGetOut.CycTimFluDetails> list) {
		QueryUtils.includeDisabled(Q.mold);
		List<Long> moldIds = list.stream().map(cycTimDevDetails -> cycTimDevDetails.getMoldId()).collect(Collectors.toList());
		List<MoldPart> moldParts = BeanUtils.get(MoldPartRepository.class).findAllByMoldIdIn(moldIds);

		list.stream().forEach(cycTimDevDetail -> {
			FltPartIn partIn = new FltPartIn();
			partIn.setMoldId(Arrays.asList(cycTimDevDetail.getMoldId()));
			List<FltPart> parts = BeanUtils.get(FltService.class).getParts(partIn, PageRequest.of(0, 999)).getContent();
			List<CycTimFluPart> cycTimDevParts = parts.stream().map(part -> {
				Integer cavity = moldParts.stream().filter(mp -> mp.getMoldId().equals(cycTimDevDetail.getMoldId()) && mp.getPartId().equals(part.getId()))
						.map(mp -> mp.getCavity()).findFirst().orElse(0);
				return new CycTimFluPart(part.getId(), part.getName(), part.getPartCode(), cavity);
			}).collect(Collectors.toList());

			cycTimDevDetail.setParts(cycTimDevParts);
		});
	}
}
