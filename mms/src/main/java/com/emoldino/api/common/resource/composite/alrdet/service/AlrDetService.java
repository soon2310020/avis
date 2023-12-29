package com.emoldino.api.common.resource.composite.alrdet.service;

import static com.emoldino.framework.enumeration.AlertTab.ALERT;
import static com.emoldino.framework.enumeration.AlertTab.HISTORY_LOG;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.base.note.util.NoteUtils;
import com.emoldino.api.common.resource.composite.alrdet.dto.AlrDetGetIn;
import com.emoldino.api.common.resource.composite.alrdet.dto.AlrDetGetOut;
import com.emoldino.api.common.resource.composite.alrdet.dto.AlrDetItem;
import com.emoldino.api.common.resource.composite.alrdet.repository.AlrDetRepository;
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
public class AlrDetService {

	@Transactional
	public AlrDetGetOut get(AlrDetGetIn input, Pageable pageable) {
		Page<AlrDetItem> page = BeanUtils.get(AlrDetRepository.class).findAll(input, null, pageable);
		List<Tab> tabs = getTabs(input, page);
		return new AlrDetGetOut(page, tabs);
	}

	private List<Tab> getTabs(AlrDetGetIn input, Page<AlrDetItem> page) {
		return Arrays.asList(//
				getTab(input, page, ALERT.getTitle()), //
				getTab(input, page, HISTORY_LOG.getTitle()) //
		);
	}

	private static Tab getTab(AlrDetGetIn input, Page<?> page, String tabName) {
		if (tabName.equals(input.getTabName())) {
			return new Tab(tabName, page.getTotalElements(), true);
		}

		AlrDetGetIn countin = ValueUtils.map2(input, AlrDetGetIn.class);
		countin.setTabName(tabName);
		long count = BeanUtils.get(AlrDetRepository.class).count(countin);
		return new Tab(tabName, count, true);
	}

	public void postNoteBatch(AlrDetGetIn input, BatchIn batchin, NoteIn body) {
		runBatch(input, batchin, item -> NoteUtils.post(PageType.DETACHMENT_ALERT, ObjectType.TOOLING, item.getMoldId(), body));
	}

	public void runBatch(AlrDetGetIn input, BatchIn batchin, Closure1ParamNoReturn<AlrDetItem> closure) {
		DataUtils.runBatch(Sort.unsorted(), 100, true, //
				pageable -> BeanUtils.get(AlrDetRepository.class).findAll(input, batchin, pageable), //
				item -> TranUtils.doNewTran(() -> closure.execute(item))//
		);
	}

}
