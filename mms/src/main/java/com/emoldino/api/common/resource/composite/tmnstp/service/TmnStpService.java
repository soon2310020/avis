package com.emoldino.api.common.resource.composite.tmnstp.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.base.tab.util.TabUtils;
import com.emoldino.api.common.resource.base.note.util.NoteUtils;
import com.emoldino.api.common.resource.composite.tmnstp.dto.TmnStpGetIn;
import com.emoldino.api.common.resource.composite.tmnstp.dto.TmnStpGetOut;
import com.emoldino.api.common.resource.composite.tmnstp.dto.TmnStpItem;
import com.emoldino.api.common.resource.composite.tmnstp.repository.TmnStpRepository;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.Tab;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.Closure1ParamNoReturn;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.emoldino.framework.util.LogicUtils;

import saleson.api.terminal.TerminalRepository;
import saleson.api.terminal.TerminalService;
import saleson.api.versioning.service.VersioningService;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.PageType;
import saleson.model.TabTable;
import saleson.model.Terminal;

@Service
public class TmnStpService {

	@Transactional
	public TmnStpGetOut get(TmnStpGetIn input, Pageable pageable) {
		Page<TmnStpItem> page = BeanUtils.get(TmnStpRepository.class).findAll(input, null, pageable);
		List<Tab> tabs = QueryUtils.findTabs(ObjectType.TERMINAL, input, page, //
				countin -> BeanUtils.get(TmnStpRepository.class).count(countin));
		return new TmnStpGetOut(page, tabs);
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
			Terminal data = findById(id);
			data.setEnabled(enabled);
			save(data);
		}
	}

	private Terminal findById(Long id) {
		return BeanUtils.get(TerminalRepository.class).findById(id).orElseThrow(() -> DataUtils.newDataNotFoundException(Terminal.class, "id", id));
	}

	private void save(Terminal data) {
		BeanUtils.get(TerminalService.class).save(data);
		Terminal dataGet = BeanUtils.get(TerminalService.class).findById(data.getId());
		BeanUtils.get(VersioningService.class).writeHistory(dataGet);
	}

	public void disableBatch(TmnStpGetIn input, BatchIn batchin) {
		saveEnabledBatch(input, batchin, false);
	}

	public void enableBatch(TmnStpGetIn input, BatchIn batchin) {
		saveEnabledBatch(input, batchin, true);
	}

	private void saveEnabledBatch(TmnStpGetIn input, BatchIn batchin, Boolean enabled) {
		ValueUtils.assertNotEmpty(enabled, "enabled");

		runBatch(input, batchin, item -> {
			Terminal terminal = findById(item.getId());
			terminal.setEnabled(enabled);
			save(terminal);
		});
	}

	public void postNoteBatch(TmnStpGetIn input, BatchIn batchin, NoteIn body) {
		runBatch(input, batchin, item -> NoteUtils.post(PageType.TERMINAL_SETTING, item.getId(), body));
	}

	public void runBatch(TmnStpGetIn input, BatchIn batchin, Closure1ParamNoReturn<TmnStpItem> closure) {
		DataUtils.runBatch(Sort.unsorted(), 100, true, //
				pageable -> BeanUtils.get(TmnStpRepository.class).findAll(input, batchin, pageable), //
				item -> TranUtils.doNewTran(() -> closure.execute(item))//
		);
	}

	public void deleteTabItemsBatch(TmnStpGetIn input, BatchIn batchin) {
		LogicUtils.assertNotNull(input.getTabName(), "tabName");
		TabTable fromTab = QueryUtils.findTabTable(ObjectType.TERMINAL, input.getTabName());
		runContentBatch(input, batchin, itemIds -> TabUtils.deleteAllData(itemIds, fromTab), false);
	}

	private void runContentBatch(TmnStpGetIn input, BatchIn batchin, Closure1ParamNoReturn<List<Long>> closure, boolean pageUpRequired) {
		DataUtils.runContentBatch(Sort.unsorted(), 100, pageUpRequired, //
				pageable -> BeanUtils.get(TmnStpRepository.class).findAll(input, batchin, pageable), //
				items -> TranUtils.doNewTran(() -> closure.execute(items.stream().map(ob -> ob.getId()).collect(Collectors.toList())))//
		);
	}

	public void moveTabItemsBatch(TmnStpGetIn input, BatchIn batchin, String toTabName) {
		LogicUtils.assertNotNull(input.getTabName(), "tabName");
		TabTable fromTab = QueryUtils.findTabTable(ObjectType.TERMINAL, input.getTabName());
		TabTable toTab = QueryUtils.findTabTable(ObjectType.TERMINAL, toTabName);
		if (toTab == null) {
			return;
		}
		runContentBatch(input, batchin, itemIds -> TabUtils.moveAllData(itemIds, fromTab, toTab), false);
	}
}
