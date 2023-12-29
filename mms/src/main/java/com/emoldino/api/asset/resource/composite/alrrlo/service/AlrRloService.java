package com.emoldino.api.asset.resource.composite.alrrlo.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.asset.resource.composite.alrrlo.dto.AlrRloGetIn;
import com.emoldino.api.asset.resource.composite.alrrlo.dto.AlrRloGetOut;
import com.emoldino.api.asset.resource.composite.alrrlo.dto.AlrRloItem;
import com.emoldino.api.asset.resource.composite.alrrlo.repository.AlrRloRepository;
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

import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.PageType;

@Service
public class AlrRloService {

	@Transactional
	public AlrRloGetOut get(AlrRloGetIn input, Pageable pageable) {
		Page<AlrRloItem> page = BeanUtils.get(AlrRloRepository.class).findAll(input, null, pageable);
		List<Tab> tabs = getTabs(input, page);
		return new AlrRloGetOut(page, tabs);
	}

	private List<Tab> getTabs(AlrRloGetIn input, Page<AlrRloItem> page) {
		return Arrays.asList( //
				getTab(input, page, AlertTab.ALERT.getTitle()), //
				getTab(input, page, AlertTab.HISTORY_LOG.getTitle()) //
		);
	}

	private static Tab getTab(AlrRloGetIn input, Page<?> page, String tabName) {
		if (tabName.equals(input.getTabName())) {
			return new Tab(tabName, page.getTotalElements(), true);
		}

		AlrRloGetIn countin = ValueUtils.map2(input, AlrRloGetIn.class);
		countin.setTabName(tabName);
		long count = BeanUtils.get(AlrRloRepository.class).count(countin);
		return new Tab(tabName, count, true);
	}

	public void postNoteBatch(AlrRloGetIn input, BatchIn batchin, NoteIn body) {
		runBatch(input, batchin, item -> NoteUtils.post(PageType.RELOCATION_ALERT, ObjectType.TOOLING, item.getMoldId(), body));
	}

	public void runBatch(AlrRloGetIn input, BatchIn batchin, Closure1ParamNoReturn<AlrRloItem> closure) {
		DataUtils.runBatch(Sort.unsorted(), 100, true, //
				pageable -> BeanUtils.get(AlrRloRepository.class).findAll(input, batchin, pageable), //
				item -> TranUtils.doNewTran(() -> closure.execute(item))//
		);
	}

}
