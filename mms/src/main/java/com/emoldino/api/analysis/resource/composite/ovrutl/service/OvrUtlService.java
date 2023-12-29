package com.emoldino.api.analysis.resource.composite.ovrutl.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevPart;
import com.emoldino.api.analysis.resource.composite.ovrutl.dto.OvrUtlDetailsGetIn;
import com.emoldino.api.analysis.resource.composite.ovrutl.dto.OvrUtlDetailsGetOut;
import com.emoldino.api.analysis.resource.composite.ovrutl.dto.OvrUtlDetailsGetOut.OvrUtlDetail;
import com.emoldino.api.analysis.resource.composite.ovrutl.dto.OvrUtlGetIn;
import com.emoldino.api.analysis.resource.composite.ovrutl.dto.OvrUtlGetOut;
import com.emoldino.api.analysis.resource.composite.ovrutl.dto.OvrUtlPart;
import com.emoldino.api.analysis.resource.composite.ovrutl.dto.OvrUtlGetOut.OvrUtlItem;
import com.emoldino.api.analysis.resource.composite.ovrutl.repository.OvrUtlRepository;
import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingUtilizationStatus;
import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.common.resource.base.option.dto.UtilizationConfig;
import com.emoldino.api.common.resource.composite.flt.dto.FltCompany;
import com.emoldino.api.common.resource.composite.flt.dto.FltIn;
import com.emoldino.api.common.resource.composite.flt.dto.FltPart;
import com.emoldino.api.common.resource.composite.flt.dto.FltPartIn;
import com.emoldino.api.common.resource.composite.flt.dto.FltPlant;
import com.emoldino.api.common.resource.composite.flt.dto.FltPlantIn;
import com.emoldino.api.common.resource.composite.flt.service.FltService;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ValueUtils;

import saleson.api.mold.MoldPartRepository;
import saleson.model.MoldPart;

@Service
@Transactional
public class OvrUtlService {

	public OvrUtlGetOut get(OvrUtlGetIn input, Pageable pageable) {
		UtilizationConfig config = MoldUtils.getUtilizationConfig();
		Page<OvrUtlItem> page = BeanUtils.get(OvrUtlRepository.class).findAll(input, config, pageable);
		page.getContent().forEach(item -> item.setPlants(getPlants(item.getSupplierId(), input.getFilterCode())));
		OvrUtlGetOut output = new OvrUtlGetOut(page.getContent(), pageable, page.getTotalElements(), config);
		return output;
	}

	private List<FltPlant> getPlants(Long supplierId, String filterCode) {
		FltPlantIn reqin = new FltPlantIn();
		reqin.setFilterCode(filterCode);
		reqin.setSupplierId(Arrays.asList(supplierId));
		Page<FltPlant> page = BeanUtils.get(FltService.class).getPlants(reqin, PageRequest.of(0, 100));
		return page.getContent();
	}

	public OvrUtlDetailsGetOut getDetails(OvrUtlDetailsGetIn input, Pageable pageable) {
		ValueUtils.assertNotEmpty(input.getSupplierId(), "supplier");

		Page<OvrUtlDetail> page = BeanUtils.get(OvrUtlRepository.class).findAllDetails(input, pageable);

		UtilizationConfig config = MoldUtils.getUtilizationConfig();
		page.forEach(detail -> {
			if (detail.getUtilizationRate() <= config.getLow()) {
				detail.setUtilizationStatus(ToolingUtilizationStatus.LOW);
			} else if (detail.getUtilizationRate() <= config.getMedium()) {
				detail.setUtilizationStatus(ToolingUtilizationStatus.MEDIUM);
			} else if (detail.getUtilizationRate() <= config.getHigh()) {
				detail.setUtilizationStatus(ToolingUtilizationStatus.HIGH);
			} else {
				detail.setUtilizationStatus(ToolingUtilizationStatus.PROLONGED);
			}
		});

		FltCompany[] supplier = { null };
		{
			FltIn reqin = new FltIn();
			reqin.setId(input.getSupplierId());
			BeanUtils.get(FltService.class).getSuppliers(reqin, PageRequest.of(0, 1)).forEach(item -> supplier[0] = item);
		}

		loadPart(page.getContent());

		OvrUtlDetailsGetOut output = new OvrUtlDetailsGetOut(page.getContent(), pageable, page.getTotalElements(), supplier[0], null);
		return output;
	}

	public void loadPart(List<OvrUtlDetail> list) {
		QueryUtils.includeDisabled(Q.mold);
		List<Long> moldIds = list.stream().map(cycTimDevDetails -> cycTimDevDetails.getMoldId()).collect(Collectors.toList());
		List<MoldPart> moldParts = BeanUtils.get(MoldPartRepository.class).findAllByMoldIdIn(moldIds);

		list.stream().forEach(ovrUtlDetail -> {
			FltPartIn partIn = new FltPartIn();
			partIn.setMoldId(Arrays.asList(ovrUtlDetail.getMoldId()));
			List<FltPart> parts = BeanUtils.get(FltService.class).getParts(partIn, PageRequest.of(0, 999)).getContent();
			List<OvrUtlPart> ovrUtlParts = parts.stream().map(part -> {
				Integer cavity = moldParts.stream().filter(mp -> mp.getMoldId().equals(ovrUtlDetail.getMoldId()) && mp.getPartId().equals(part.getId())).map(mp -> mp.getCavity())
						.findFirst().orElse(0);
				return new OvrUtlPart(part.getId(), part.getName(), part.getPartCode(), cavity);
			}).collect(Collectors.toList());

			ovrUtlDetail.setParts(ovrUtlParts);
		});
	}

}
