package com.emoldino.api.asset.resource.composite.alreol.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.asset.resource.composite.alreol.dto.AlrEolGetIn;
import com.emoldino.api.asset.resource.composite.alreol.dto.AlrEolGetOut;
import com.emoldino.api.asset.resource.composite.alreol.dto.AlrEolItem;
import com.emoldino.api.asset.resource.composite.alreol.enumeration.AlrEolTab;
import com.emoldino.api.asset.resource.composite.alreol.repository.AlrEolRepository;
import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.base.note.util.NoteUtils;
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
public class AlrEolService {

	@Transactional
	public AlrEolGetOut get(AlrEolGetIn input, Pageable pageable) {
		Page<AlrEolItem> page = BeanUtils.get(AlrEolRepository.class).findAll(input, null, pageable);
		List<Tab> tabs = getTabs(input, page);
		return new AlrEolGetOut(page, tabs);
	}

	private static List<Tab> getTabs(AlrEolGetIn input, Page<?> page) {
		return Arrays.asList(//
				getTab(input, page, AlertTab.ALERT.getTitle()), //
				getTab(input, page, AlrEolTab.APPROVED.getTitle()), //
				getTab(input, page, AlrEolTab.DISAPPROVED.getTitle()), //
				getTab(input, page, AlertTab.HISTORY_LOG.getTitle())//
		);
	}

	private static Tab getTab(AlrEolGetIn input, Page<?> page, String tabName) {
		if (tabName.equals(input.getTabName())) {
			return new Tab(tabName, page.getTotalElements(), true);
		}

		AlrEolGetIn countin = ValueUtils.map2(input, AlrEolGetIn.class);
		countin.setTabName(tabName);
		long count = BeanUtils.get(AlrEolRepository.class).count(countin);
		return new Tab(tabName, count, true);
	}

	public void postNoteBatch(AlrEolGetIn input, BatchIn batchin, NoteIn body) {
		runBatch(input, batchin, item -> NoteUtils.post(PageType.REFURBISHMENT_ALERT, item.getMoldId(), body));
	}

	public void runBatch(AlrEolGetIn input, BatchIn batchin, Closure1ParamNoReturn<AlrEolItem> closure) {
		DataUtils.runBatch(Sort.unsorted(), 100, true, //
				pageable -> BeanUtils.get(AlrEolRepository.class).findAll(input, batchin, pageable), //
				item -> TranUtils.doNewTran(() -> closure.execute(item))//
		);
	}

}
