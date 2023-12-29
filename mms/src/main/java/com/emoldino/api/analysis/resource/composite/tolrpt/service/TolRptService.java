package com.emoldino.api.analysis.resource.composite.tolrpt.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.analysis.resource.composite.tolrpt.dto.TolRptGetIn;
import com.emoldino.api.analysis.resource.composite.tolrpt.dto.TolRptItem;
import com.emoldino.api.analysis.resource.composite.tolrpt.dto.TolRptItem.TolRptPart;
import com.emoldino.api.analysis.resource.composite.tolrpt.repository.TolRptRepository;
import com.emoldino.api.common.resource.base.object.util.EmObjectUtils;
import com.emoldino.api.common.resource.composite.datexp.util.DatExpUtils;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ValueUtils;

import saleson.common.enumeration.ObjectType;

@Service
@Transactional
public class TolRptService {

	public Page<TolRptItem> get(TolRptGetIn input, BatchIn batchin, Pageable pageable) {
		Page<TolRptItem> page = BeanUtils.get(TolRptRepository.class).findAll(input, batchin, pageable);
		loadCustomFieldValues(page);
		loadParts(page);
		return page;
	}

	private void loadCustomFieldValues(Page<TolRptItem> page) {
		Map<Long, TolRptItem> map = page.getContent().stream()//
				.collect(Collectors.toMap(TolRptItem::getMoldId, Function.identity(), (v1, v2) -> v1, LinkedHashMap::new));
		EmObjectUtils.loadCustomFieldValues(ObjectType.TOOLING, //
				new ArrayList<>(map.keySet()), (objectId, fields) -> map.get(objectId).setCustomFields(fields));
	}

	private void loadParts(Page<TolRptItem> page) {
		for (TolRptItem item : page.getContent()) {
			List<TolRptPart> parts = BeanUtils.get(TolRptRepository.class)//
					.findPartsByMoldId(item.getMoldId())//
					.stream()//
					.map(part -> ValueUtils.map(part, TolRptPart.class))//
					.collect(Collectors.toList());
			item.setParts(parts);
		}
	}

	public void export(TolRptGetIn input, BatchIn batchin, HttpServletResponse response) {
		DatExpUtils.exportByJxls(//
				"TOL_RPT", //
				pageable -> BeanUtils.get(TolRptService.class).get(input, batchin, pageable), //
				100, Sort.unsorted(), //
				"Tooling Report", response//
		);
	}

}
