package com.emoldino.api.common.resource.composite.alrdco.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.base.note.util.NoteUtils;
import com.emoldino.api.common.resource.composite.alrdco.dto.AlrDcoGetIn;
import com.emoldino.api.common.resource.composite.alrdco.dto.AlrDcoTerminal;
import com.emoldino.api.common.resource.composite.alrdco.dto.AlrDcoTerminalsGetOut;
import com.emoldino.api.common.resource.composite.alrdco.dto.AlrDcoTooling;
import com.emoldino.api.common.resource.composite.alrdco.dto.AlrDcoToolingsGetOut;
import com.emoldino.api.common.resource.composite.alrdco.repository.AlrDcoRepository;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.Tab;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.Closure1ParamNoReturn;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;

import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.PageType;

@Service
public class AlrDcoService {

	@Transactional
	public AlrDcoTerminalsGetOut getTerminals(AlrDcoGetIn input, Pageable pageable) {
		Page<AlrDcoTerminal> page = BeanUtils.get(AlrDcoRepository.class).findAllTerminals(input, null, pageable);
		List<Tab> tabs = getTabs(input, page);
		return new AlrDcoTerminalsGetOut(page, tabs);
	}

	@Transactional
	public AlrDcoToolingsGetOut getToolings(AlrDcoGetIn input, Pageable pageable) {
		Page<AlrDcoTooling> page = BeanUtils.get(AlrDcoRepository.class).findAllToolings(input, null, pageable);
		List<Tab> tabs = getTabs(input, page);
		return new AlrDcoToolingsGetOut(page, tabs);
	}

	private static List<Tab> getTabs(AlrDcoGetIn input, Page<?> page) {
		return Arrays.asList(//
				getTab(input, page, "Terminal Disconnection"), //
				getTab(input, page, "Tooling Disconnection"), //
				getTab(input, page, "Terminal Disconnection History"), //
				getTab(input, page, "Tooling Disconnection History")//
		);
	}

	private static Tab getTab(AlrDcoGetIn input, Page<?> page, String tabName) {
		if (tabName.equals(input.getTabName())) {
			return new Tab(tabName, page.getTotalElements(), true);
		}

		AlrDcoGetIn countin = ValueUtils.map2(input, AlrDcoGetIn.class);
		countin.setTabName(tabName);
		long count = tabName.startsWith("Terminal") ? BeanUtils.get(AlrDcoRepository.class).countTerminals(countin) : BeanUtils.get(AlrDcoRepository.class).countToolings(countin);
		return new Tab(tabName, count, true);
	}

	public void postTerminalsNoteBatch(AlrDcoGetIn input, BatchIn batchin, NoteIn body) {
		runTerminalsBatch(input, batchin, item -> NoteUtils.post(PageType.DISCONNECTION_ALERT, ObjectType.TERMINAL, item.getTerminalId(), body));
	}

	private void runTerminalsBatch(AlrDcoGetIn input, BatchIn batchin, Closure1ParamNoReturn<AlrDcoTerminal> closure) {
		DataUtils.runBatch(Sort.unsorted(), 100, true, //
				pageable -> BeanUtils.get(AlrDcoRepository.class).findAllTerminals(input, batchin, pageable), //
				item -> TranUtils.doNewTran(() -> closure.execute(item))//
		);
	}

	public void postToolingsNoteBatch(AlrDcoGetIn input, BatchIn batchin, NoteIn body) {
		runToolingsBatch(input, batchin, item -> NoteUtils.post(PageType.DISCONNECTION_ALERT, ObjectType.TOOLING, item.getMoldId(), body));
	}

	private void runToolingsBatch(AlrDcoGetIn input, BatchIn batchin, Closure1ParamNoReturn<AlrDcoTooling> closure) {
		DataUtils.runBatch(Sort.unsorted(), 100, true, //
				pageable -> BeanUtils.get(AlrDcoRepository.class).findAllToolings(input, batchin, pageable), //
				item -> TranUtils.doNewTran(() -> closure.execute(item))//
		);
	}

}
