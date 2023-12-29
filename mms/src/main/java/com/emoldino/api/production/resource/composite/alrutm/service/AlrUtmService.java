package com.emoldino.api.production.resource.composite.alrutm.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.base.note.util.NoteUtils;
import com.emoldino.api.production.resource.composite.alrutm.dto.AlrUtmGetIn;
import com.emoldino.api.production.resource.composite.alrutm.dto.AlrUtmGetOut;
import com.emoldino.api.production.resource.composite.alrutm.dto.AlrUtmItem;
import com.emoldino.api.production.resource.composite.alrutm.enumeration.AlrUtmTab;
import com.emoldino.api.production.resource.composite.alrutm.repository.AlrUtmRepository;
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
public class AlrUtmService {

	@Transactional
	public AlrUtmGetOut get(AlrUtmGetIn input, Pageable pageable) {
		Page<AlrUtmItem> page = BeanUtils.get(AlrUtmRepository.class).findAll(input, null, pageable);
		List<Tab> tabs = getTabs(input, page);
		return new AlrUtmGetOut(page, tabs);
	}

	private static List<Tab> getTabs(AlrUtmGetIn input, Page<?> page) {
		List<Tab> tabs = new ArrayList<>();
		tabs.add(getTab(input, page, AlertTab.ALERT.getTitle()));
		tabs.add(getTab(input, page, AlrUtmTab.OUTSIDE_L1.getTitle()));
		tabs.add(getTab(input, page, AlrUtmTab.OUTSIDE_L2.getTitle()));
		tabs.add(getTab(input, page, AlertTab.HISTORY_LOG.getTitle()));
		return tabs;
	}

	private static Tab getTab(AlrUtmGetIn input, Page<?> page, String tabName) {
		if (tabName.equals(input.getTabName())) {
			return new Tab(tabName, page.getTotalElements(), true);
		}

		AlrUtmGetIn countin = ValueUtils.map2(input, AlrUtmGetIn.class);
		countin.setTabName(tabName);
		long count = BeanUtils.get(AlrUtmRepository.class).count(countin);
		return new Tab(tabName, count, true);
	}

	public void postNoteBatch(AlrUtmGetIn input, BatchIn batchin, NoteIn body) {
		runBatch(input, batchin, item -> NoteUtils.post(PageType.UPTIME_ALERT, item.getMoldId(), body));
	}

	public void runBatch(AlrUtmGetIn input, BatchIn batchin, Closure1ParamNoReturn<AlrUtmItem> closure) {
		DataUtils.runBatch(Sort.unsorted(), 100, true, //
				pageable -> BeanUtils.get(AlrUtmRepository.class).findAll(input, batchin, pageable), //
				item -> TranUtils.doNewTran(() -> closure.execute(item))//
		);
	}

}
