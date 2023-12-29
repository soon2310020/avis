package com.emoldino.api.production.resource.composite.alrcyctim.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.base.note.util.NoteUtils;
import com.emoldino.api.production.resource.composite.alrcyctim.dto.AlrCycTimGetIn;
import com.emoldino.api.production.resource.composite.alrcyctim.dto.AlrCycTimGetOut;
import com.emoldino.api.production.resource.composite.alrcyctim.dto.AlrCycTimItem;
import com.emoldino.api.production.resource.composite.alrcyctim.enumeration.AlrCycTimTab;
import com.emoldino.api.production.resource.composite.alrcyctim.repository.AlrCycTimRepository;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.Tab;
import com.emoldino.framework.enumeration.AlertTab;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.Closure1ParamNoReturn;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;

import saleson.common.enumeration.PageType;

@Service
public class AlrCycTimService {
	@Transactional
	public AlrCycTimGetOut get(AlrCycTimGetIn input, Pageable pageable) {
		Page<AlrCycTimItem> page = BeanUtils.get(AlrCycTimRepository.class).findAll(input, null, pageable);
		List<Tab> tabs = getTabs(input, page);
		return new AlrCycTimGetOut(page, tabs);
	}

	private static List<Tab> getTabs(AlrCycTimGetIn input, Page<?> page) {
		List<Tab> tabs = new ArrayList<>();
		tabs.add(getTab(input, page, AlertTab.ALERT.getTitle()));
		tabs.add(getTab(input, page, AlrCycTimTab.OUTSIDE_L1.getTitle()));
		tabs.add(getTab(input, page, AlrCycTimTab.OUTSIDE_L2.getTitle()));
		tabs.add(getTab(input, page, AlertTab.HISTORY_LOG.getTitle()));
		return tabs;
	}

	private static Tab getTab(AlrCycTimGetIn input, Page<?> page, String tabName) {
		if (tabName.equals(input.getTabName())) {
			return new Tab(tabName, page.getTotalElements(), true);
		}

		AlrCycTimGetIn counting = ValueUtils.map2(input, AlrCycTimGetIn.class);
		counting.setTabName(tabName);
		long count = BeanUtils.get(AlrCycTimRepository.class).count(counting);
		return new Tab(tabName, count, true);
	}

	public void postNoteBatch(AlrCycTimGetIn input, BatchIn batchin, NoteIn body) {
		runBatch(input, batchin, item -> NoteUtils.post(PageType.CYCLE_TIME_ALERT, item.getMoldId(), body));
	}

	public void runBatch(AlrCycTimGetIn input, BatchIn batchin, Closure1ParamNoReturn<AlrCycTimItem> closure) {
		DataUtils.runBatch(Sort.unsorted(), 100, true, //
				pageable -> BeanUtils.get(AlrCycTimRepository.class).findAll(input, batchin, pageable), //
				item -> TranUtils.doNewTran(() -> closure.execute(item))//
		);
	}
}
