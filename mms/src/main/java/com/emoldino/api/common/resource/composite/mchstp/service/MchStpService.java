package com.emoldino.api.common.resource.composite.mchstp.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import com.emoldino.api.common.resource.base.tab.util.TabUtils;
import com.emoldino.api.common.resource.composite.datexp.util.DatExpUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.base.object.util.EmObjectUtils;
import com.emoldino.api.common.resource.composite.mchstp.dto.MchStpGetIn;
import com.emoldino.api.common.resource.composite.mchstp.dto.MchStpGetOut;
import com.emoldino.api.common.resource.composite.mchstp.dto.MchStpItem;
import com.emoldino.api.common.resource.composite.mchstp.repository.MchStpRepository;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.Tab;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.Closure1ParamNoReturn;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.ValueUtils;

import saleson.api.machine.MachineMoldMatchingHistoryRepository;
import saleson.api.machine.MachineRepository;
import saleson.api.machine.MachineService;
import saleson.api.mold.MoldRepository;
import saleson.api.systemNote.SystemNoteService;
import saleson.api.systemNote.payload.SystemNotePayload;
import saleson.api.versioning.service.VersioningService;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.PageType;
import saleson.model.Machine;
import saleson.model.MachineMoldMatchingHistory;
import saleson.model.Mold;
import saleson.model.TabTable;

@Service
public class MchStpService {

	@Transactional
	public MchStpGetOut get(MchStpGetIn input, Pageable pageable) {
		Page<MchStpItem> page = BeanUtils.get(MchStpRepository.class).findAll(input, null, pageable);
		loadCustomFieldValues(page);
		List<Tab> tabs = QueryUtils.findTabs(ObjectType.MACHINE, input, page, //
				countin -> BeanUtils.get(MchStpRepository.class).count(countin));
		return new MchStpGetOut(page, tabs);
	}

	private Page<MchStpItem> get(MchStpGetIn input, BatchIn batchin, Pageable pageable) {
		Page<MchStpItem> page = BeanUtils.get(MchStpRepository.class).findAll(input, batchin, pageable);
		loadCustomFieldValues(page);
		return page;
	}

	private void loadCustomFieldValues(Page<MchStpItem> page) {
		Map<Long, MchStpItem> map = page.getContent().stream().collect(Collectors.toMap(MchStpItem::getId, Function.identity(), (v1, v2) -> v1, LinkedHashMap::new));
		EmObjectUtils.loadCustomFieldValues(ObjectType.MACHINE, new ArrayList<>(map.keySet()), (objectId, fields) -> map.get(objectId).setCustomFields(fields));
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
			Machine data = findById(id);
			data.setEnabled(enabled);
			save(data);
		}
	}

	private Machine findById(Long id) {
		return BeanUtils.get(MachineRepository.class).findById(id).orElseThrow(() -> DataUtils.newDataNotFoundException(Machine.class, "id", id));
	}

	private void save(Machine data) {
		BeanUtils.get(MachineService.class).save(data);
		Machine dataGet = BeanUtils.get(MachineService.class).findById(data.getId());
		BeanUtils.get(VersioningService.class).writeHistory(dataGet);
	}

	public void disableBatch(MchStpGetIn input, BatchIn batchin) {
		saveEnabledBatch(input, batchin, false);
	}

	public void enableBatch(MchStpGetIn input, BatchIn batchin) {
		saveEnabledBatch(input, batchin, true);
	}

	private void saveEnabledBatch(MchStpGetIn input, BatchIn batchin, Boolean enabled) {
		ValueUtils.assertNotEmpty(enabled, "enabled");

		runBatch(input, batchin, item -> {
			Machine machine = findById(item.getId());
			machine.setEnabled(enabled);
			save(machine);
		});
	}

	public void postNoteBatch(MchStpGetIn input, BatchIn batchin, NoteIn body) {
		SystemNotePayload payload = new SystemNotePayload();
		payload.setSystemNoteFunction(PageType.MACHINE_SETTING);
		payload.setMessage(body.getMessage());
		payload.setSystemNoteParamList(body.getParams());

		runBatch(input, batchin, item -> {
			payload.setObjectFunctionId(item.getId());
			BeanUtils.get(SystemNoteService.class).create(payload);
		});
	}

	public void unmatchBatch(MchStpGetIn input, BatchIn batchin) {
		runBatch(input, batchin, item -> {
			Machine machine = findById(item.getId());
			TranUtils.doNewTran(() -> unmatch(machine));
		});
	}

	private void unmatch(Machine machine) {
		if (machine == null) {
			return;
		}
		Mold mold = machine.getMold();
		if (mold == null) {
			return;
		}

		mold.setMachineId(null);
		mold.setMachine(null);
		BeanUtils.get(MoldRepository.class).save(mold);

		machine.setMold(null);
		save(machine);

		MachineMoldMatchingHistory history = BeanUtils.get(MachineMoldMatchingHistoryRepository.class)
				.findFirstByMachineAndMatchDayIsNotNullAndCompletedIsFalseOrderByMatchTimeDesc(machine).orElse(null);
		if (history == null) {
			history = new MachineMoldMatchingHistory();
			history.setMachine(machine);
			history.setMold(mold);
		}
		Instant unmatchTime = DateUtils2.getInstant();
		history.setUnmatchTime(unmatchTime);
		history.setUnmatchDay(DateUtils2.format(unmatchTime, DateUtils2.DatePattern.yyyyMMdd, LocationUtils.getZoneIdByLocationId(machine.getLocationId()), null));
		history.setUnmatchHour(DateUtils2.format(unmatchTime, "HHmm", LocationUtils.getZoneIdByLocationId(machine.getLocationId()), null));
		history.setCompleted(true);
		BeanUtils.get(MachineMoldMatchingHistoryRepository.class).save(history);
	}

	private void runBatch(MchStpGetIn input, BatchIn batchin, Closure1ParamNoReturn<MchStpItem> closure) {
		DataUtils.runBatch(Sort.unsorted(), 100, true, //
				pageable -> BeanUtils.get(MchStpRepository.class).findAll(input, batchin, pageable), //
				item -> TranUtils.doNewTran(() -> closure.execute(item))//
		);
	}

	public void export(MchStpGetIn input, BatchIn batchin, Sort sort, HttpServletResponse response) {
		DatExpUtils.exportByJxls("MACHINE", //
				pageable2 -> BeanUtils.get(MchStpService.class).get(input, batchin, pageable2), //
				100, sort == null || sort.isUnsorted() ? Sort.by("machineCode") : sort, //
				"Machine", response//
		);
	}

	public void deleteTabItemsBatch(MchStpGetIn input, BatchIn batchin) {
		LogicUtils.assertNotNull(input.getTabName(), "tabName");
		TabTable fromTab = QueryUtils.findTabTable(ObjectType.MACHINE, input.getTabName());
		runContentBatch(input, batchin, itemIds -> TabUtils.deleteAllData(itemIds, fromTab), false);
	}

	private void runContentBatch(MchStpGetIn input, BatchIn batchin, Closure1ParamNoReturn<List<Long>> closure, boolean pageUpRequired) {
		DataUtils.runContentBatch(Sort.unsorted(), 100, pageUpRequired, //
				pageable -> BeanUtils.get(MchStpRepository.class).findAll(input, batchin, pageable), //
				items -> TranUtils.doNewTran(() -> closure.execute(items.stream().map(ob -> ob.getId()).collect(Collectors.toList())))//
		);
	}

	public void moveTabItemsBatch(MchStpGetIn input, BatchIn batchin, String toTabName) {
		LogicUtils.assertNotNull(input.getTabName(), "tabName");
		TabTable fromTab = QueryUtils.findTabTable(ObjectType.MACHINE, input.getTabName());
		TabTable toTab = QueryUtils.findTabTable(ObjectType.MACHINE, toTabName);
		if (toTab == null) {
			return;
		}
		runContentBatch(input, batchin, itemIds -> TabUtils.moveAllData(itemIds, fromTab, toTab), false);
	}
}
