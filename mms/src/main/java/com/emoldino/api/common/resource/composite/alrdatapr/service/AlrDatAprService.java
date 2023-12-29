package com.emoldino.api.common.resource.composite.alrdatapr.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.base.note.util.NoteUtils;
import com.emoldino.api.common.resource.composite.alrdatapr.dto.AlrDatAprGetIn;
import com.emoldino.api.common.resource.composite.alrdatapr.dto.AlrDatAprGetOut;
import com.emoldino.api.common.resource.composite.alrdatapr.dto.AlrDatAprItem;
import com.emoldino.api.common.resource.composite.alrdatapr.enumeration.DatAprTab;
import com.emoldino.api.common.resource.composite.alrdatapr.repository.AlrDatAprRepository;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.Tab;
import com.emoldino.framework.enumeration.AlertTab;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.Closure1ParamNoReturn;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;

import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.PageType;

@Service
public class AlrDatAprService {

	@Transactional
	public AlrDatAprGetOut get(AlrDatAprGetIn input, Pageable pageable) {
		Page<AlrDatAprItem> page = BeanUtils.get(AlrDatAprRepository.class).findAll(input, null, pageable);
		List<Tab> tabs = getTabs(input, page);
		return new AlrDatAprGetOut(page, tabs);
	}

	private List<Tab> getTabs(AlrDatAprGetIn input, Page<AlrDatAprItem> page) {
		List<Tab> tabs = new ArrayList<>();
		tabs.add(getTab(input, page, AlertTab.ALERT.getTitle()));
		/*
		 * If User is not OEM
		 */
		if (AccessControlUtils.isAccessFilterRequired()) {
			tabs.add(getTab(input, page, DatAprTab.APPROVED.getTitle()));
			tabs.add(getTab(input, page, DatAprTab.DISAPPROVED.getTitle()));
		}
		tabs.add(getTab(input, page, AlertTab.HISTORY_LOG.getTitle()));

		return tabs;
	}

	private static Tab getTab(AlrDatAprGetIn input, Page<?> page, String tabName) {
		if (tabName.equals(input.getTabName())) {
			return new Tab(tabName, page.getTotalElements(), true);
		}

		AlrDatAprGetIn countin = ValueUtils.map2(input, AlrDatAprGetIn.class);
		countin.setTabName(tabName);
		long count = BeanUtils.get(AlrDatAprRepository.class).count(countin);
		return new Tab(tabName, count, true);
	}

	public void postNoteBatch(AlrDatAprGetIn input, BatchIn batchin, NoteIn body) {
		runBatch(input, batchin, item -> NoteUtils.post(PageType.DATA_SUBMISSION_ALERT, ObjectType.TOOLING, item.getMoldId(), body));
	}

	public void runBatch(AlrDatAprGetIn input, BatchIn batchin, Closure1ParamNoReturn<AlrDatAprItem> closure) {
		DataUtils.runBatch(Sort.unsorted(), 100, true, //
				pageable -> BeanUtils.get(AlrDatAprRepository.class).findAll(input, batchin, pageable), //
				item -> TranUtils.doNewTran(() -> closure.execute(item))//
		);
	}

}
