package com.emoldino.api.common.resource.composite.parstp.service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import com.emoldino.api.common.resource.base.tab.util.TabUtils;
import com.emoldino.framework.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.masterdata.repository.part2.Part2;
import com.emoldino.api.common.resource.base.masterdata.repository.part2.Part2Repository;
import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.base.note.util.NoteUtils;
import com.emoldino.api.common.resource.base.object.util.EmObjectUtils;
import com.emoldino.api.common.resource.base.option.enumeration.MasterFilterMode;
import com.emoldino.api.common.resource.composite.datexp.util.DatExpUtils;
import com.emoldino.api.common.resource.composite.parstp.dto.ParStpGetIn;
import com.emoldino.api.common.resource.composite.parstp.dto.ParStpGetOut;
import com.emoldino.api.common.resource.composite.parstp.dto.ParStpItem;
import com.emoldino.api.common.resource.composite.parstp.repository.ParStpRepository;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.Tab;

import saleson.api.part.PartRepository;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.PageType;
import saleson.common.util.ExcelUtils;
import saleson.model.Part;
import saleson.model.TabTable;

@Service
public class ParStpService {

	@Transactional
	public ParStpGetOut get(ParStpGetIn input, Pageable pageable) {
		Page<ParStpItem> page = get(input, null, pageable);
		List<Tab> tabs = QueryUtils.findTabs(ObjectType.PART, input, page, //
				countin -> BeanUtils.get(ParStpRepository.class).count(countin));
		return new ParStpGetOut(page, tabs);
	}

	private Page<ParStpItem> get(ParStpGetIn input, BatchIn batchin, Pageable pageable) {
		Page<ParStpItem> page = BeanUtils.get(ParStpRepository.class).findAll(input, batchin, pageable);
		loadCustomFieldValues(page);
		return page;
	}

	private void loadCustomFieldValues(Page<ParStpItem> page) {
		Map<Long, ParStpItem> map = page.getContent().stream().collect(Collectors.toMap(ParStpItem::getId, Function.identity(), (v1, v2) -> v1, LinkedHashMap::new));
		EmObjectUtils.loadCustomFieldValues(ObjectType.PART, new ArrayList<>(map.keySet()), (objectId, fields) -> map.get(objectId).setCustomFields(fields));
	}

	@Transactional
	public void disable(List<Long> ids) {
		saveEnabled(ids, false);
	}

	@Transactional
	public void enable(List<Long> ids) {
		saveEnabled(ids, true);
	}

	private void saveEnabled(List<Long> ids, boolean enabled) {
		if (ObjectUtils.isEmpty(ids) || ids.size() > 100) {
			return;
		}
		for (Long id : ids) {
			Part2 data = findById2(id);
			data.setEnabled(enabled);
			save2(data);
		}
	}

//	private Part findById(Long id) {
//		return BeanUtils.get(PartRepository.class).findById(id).orElseThrow(() -> DataUtils.newDataNotFoundException(Part.class, "id", id));
//	}

//	private void save(Part data) {
//		BeanUtils.get(PartService.class).save(data);
//		Part dataGet = findById(data.getId());
//		BeanUtils.get(VersioningService.class).writeHistory(dataGet);
//	}

	private Part2 findById2(Long id) {
		return BeanUtils.get(Part2Repository.class).findById(id).orElseThrow(() -> DataUtils.newDataNotFoundException(Part.class, "id", id));
	}

	private void save2(Part2 data) {
		BeanUtils.get(Part2Repository.class).save(data);
	}

	public void disableBatch(ParStpGetIn input, BatchIn batchin) {
		saveEnabledBatch(input, batchin, false);
	}

	public void enableBatch(ParStpGetIn input, BatchIn batchin) {
		saveEnabledBatch(input, batchin, true);
	}

	public void postNoteBatch(ParStpGetIn input, BatchIn batchin, NoteIn body) {
		runBatch(input, batchin, item -> NoteUtils.post(PageType.PART_SETTING, item.getId(), body), true);
	}

	private void saveEnabledBatch(ParStpGetIn input, BatchIn batchin, Boolean enabled) {
		ValueUtils.assertNotEmpty(enabled, "enabled");

		runBatch(input, batchin, item -> {
			Part2 part = findById2(item.getId());
			part.setEnabled(enabled);
			save2(part);
		}, false);
	}

	private void runBatch(ParStpGetIn input, BatchIn batchin, Closure1ParamNoReturn<ParStpItem> closure, boolean pageUpRequired) {
		DataUtils.runBatch(Sort.unsorted(), 100, pageUpRequired, //
				pageable -> BeanUtils.get(ParStpRepository.class).findAll(input, batchin, pageable), //
				item -> TranUtils.doNewTran(() -> closure.execute(item))//
		);
	}

	@Transactional
	public void export(ParStpGetIn input, BatchIn batchin, Sort sort, HttpServletResponse response) {
		DatExpUtils.exportByJxls("PART", //
				pageable2 -> BeanUtils.get(ParStpService.class).get(input, batchin, pageable2), //
				100, sort == null || sort.isUnsorted() ? Sort.by("partCode") : sort, //
				"Part", response//
		);
	}

	public ByteArrayOutputStream exportStatic(ParStpGetIn input, BatchIn batchin, String timeRange, Pageable pageable) {
		List<Part> parts = getAllMoldListForExport(input, batchin, pageable);
		return BeanUtils.get(ExcelUtils.class).exportExcelPartListNew(parts, timeRange);
	}

	private List<Part> getAllMoldListForExport(ParStpGetIn input, BatchIn batchin, Pageable pageable) {
		List<Part> partList = new ArrayList<>();

		List<Long> ids;
		Pageable pageableNew = Pageable.unpaged();
		if (pageable != null && pageable.getSort() != null) {
			if (pageable.getSort() != null) {
				pageableNew = PageRequest.of(0, Integer.MAX_VALUE, pageable.getSort());
			}
		} else {
			pageableNew = PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.ASC, "id");
		}
		if (batchin.getSelectionMode() == null || batchin.getSelectionMode().equals(MasterFilterMode.SELECTED)) {
			ids = batchin.getSelectedIds();
		} else {
			Page<ParStpItem> page = BeanUtils.get(ParStpRepository.class).findAll(input, batchin, pageableNew);
			ids = page.getContent().stream().map(ParStpItem::getId).collect(Collectors.toList());
		}
		if (ids != null)
			partList = BeanUtils.get(PartRepository.class).findAllById(ids);
//		}
		return partList;
	}

	public void deleteTabItemsBatch(ParStpGetIn input, BatchIn batchin) {
		LogicUtils.assertNotNull(input.getTabName(), "tabName");
		TabTable fromTab = QueryUtils.findTabTable(ObjectType.PART, input.getTabName());
		runContentBatch(input, batchin, itemIds -> TabUtils.deleteAllData(itemIds, fromTab), false);
	}

	private void runContentBatch(ParStpGetIn input, BatchIn batchin, Closure1ParamNoReturn<List<Long>> closure, boolean pageUpRequired) {
		DataUtils.runContentBatch(Sort.unsorted(), 100, pageUpRequired, //
				pageable -> BeanUtils.get(ParStpRepository.class).findAll(input, batchin, pageable), //
				items -> TranUtils.doNewTran(() -> closure.execute(items.stream().map(ob -> ob.getId()).collect(Collectors.toList())))//
		);
	}

	public void moveTabItemsBatch(ParStpGetIn input, BatchIn batchin, String toTabName) {
		LogicUtils.assertNotNull(input.getTabName(), "tabName");
		TabTable fromTab = QueryUtils.findTabTable(ObjectType.PART, input.getTabName());
		TabTable toTab = QueryUtils.findTabTable(ObjectType.PART, toTabName);
		if (toTab == null) {
			return;
		}
		runContentBatch(input, batchin, itemIds -> TabUtils.moveAllData(itemIds, fromTab, toTab), false);
	}
}
