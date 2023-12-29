package com.emoldino.api.common.resource.composite.alrrst.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.base.note.util.NoteUtils;
import com.emoldino.api.common.resource.composite.alrrst.dto.AlrRstGetIn;
import com.emoldino.api.common.resource.composite.alrrst.dto.AlrRstGetOut;
import com.emoldino.api.common.resource.composite.alrrst.dto.AlrRstItem;
import com.emoldino.api.common.resource.composite.alrrst.repository.AlrRstRepository;
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
public class AlrRstService {

	@Transactional
	public AlrRstGetOut get(AlrRstGetIn input, Pageable pageable) {
		Page<AlrRstItem> page = BeanUtils.get(AlrRstRepository.class).findAll(input, null, pageable);
		List<Tab> tabs = getTabs(input, page);
		return new AlrRstGetOut(page, tabs);
	}

	private static List<Tab> getTabs(AlrRstGetIn input, Page<?> page) {
		return Arrays.asList(//
				getTab(input, page, AlertTab.ALERT.getTitle()), //
				getTab(input, page, AlertTab.HISTORY_LOG.getTitle())//
		);
	}

	private static Tab getTab(AlrRstGetIn input, Page<?> page, String tabName) {
		if (tabName.equals(input.getTabName())) {
			return new Tab(tabName, page.getTotalElements(), true);
		}

		AlrRstGetIn countin = ValueUtils.map2(input, AlrRstGetIn.class);
		countin.setTabName(tabName);
		long count = BeanUtils.get(AlrRstRepository.class).count(countin);
		return new Tab(tabName, count, true);
	}

	public void postNoteBatch(AlrRstGetIn input, BatchIn batchin, NoteIn body) {
		runBatch(input, batchin, item -> NoteUtils.post(PageType.RESET_ALERT, item.getMoldId(), body));
	}

	public void runBatch(AlrRstGetIn input, BatchIn batchin, Closure1ParamNoReturn<AlrRstItem> closure) {
		DataUtils.runBatch(Sort.unsorted(), 100, true, //
				pageable -> BeanUtils.get(AlrRstRepository.class).findAll(input, batchin, pageable), //
				item -> TranUtils.doNewTran(() -> closure.execute(item))//
		);
	}

}
