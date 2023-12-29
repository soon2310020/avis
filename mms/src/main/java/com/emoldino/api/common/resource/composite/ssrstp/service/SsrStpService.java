package com.emoldino.api.common.resource.composite.ssrstp.service;

import java.util.List;
import java.util.stream.Collectors;

import com.emoldino.api.common.resource.base.tab.util.TabUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.base.note.util.NoteUtils;
import com.emoldino.api.common.resource.composite.ssrstp.dto.SsrStpGetIn;
import com.emoldino.api.common.resource.composite.ssrstp.dto.SsrStpGetOut;
import com.emoldino.api.common.resource.composite.ssrstp.dto.SsrStpItem;
import com.emoldino.api.common.resource.composite.ssrstp.dto.SsrStpPutSubscriptionTermIn;
import com.emoldino.api.common.resource.composite.ssrstp.repository.SsrStpRepository;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.Tab;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.Closure1ParamNoReturn;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.ValueUtils;

import saleson.api.counter.CounterRepository;
import saleson.api.counter.CounterService;
import saleson.api.versioning.service.VersioningService;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.PageType;
import saleson.model.Counter;
import saleson.model.TabTable;

@Service
public class SsrStpService {

	@Transactional
	public SsrStpGetOut get(SsrStpGetIn input, Pageable pageable) {
		Page<SsrStpItem> page = BeanUtils.get(SsrStpRepository.class).findAll(input, null, pageable);
		List<Tab> tabs = QueryUtils.findTabs(ObjectType.COUNTER, input, page, //
				countin -> BeanUtils.get(SsrStpRepository.class).count(countin));
		return new SsrStpGetOut(page, tabs);
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
			Counter data = findById(id);
			data.setEnabled(enabled);
			save(data);
		}
	}

	private Counter findById(Long id) {
		return BeanUtils.get(CounterRepository.class).findById(id).orElseThrow(() -> DataUtils.newDataNotFoundException(Counter.class, "id", id));
	}

	private void save(Counter data) {
		BeanUtils.get(CounterService.class).save(data);
		Counter dataGet = BeanUtils.get(CounterService.class).findById(data.getId());
		BeanUtils.get(VersioningService.class).writeHistory(dataGet);
	}

	public void disableBatch(SsrStpGetIn input, BatchIn batchin) {
		saveEnabledBatch(input, batchin, false);
	}

	public void enableBatch(SsrStpGetIn input, BatchIn batchin) {
		saveEnabledBatch(input, batchin, true);
	}

	private void saveEnabledBatch(SsrStpGetIn input, BatchIn batchin, boolean enabled) {
		ValueUtils.assertNotEmpty(enabled, "enabled");

		runBatch(input, batchin, item -> {
			Counter sensor = findById(item.getId());
			sensor.setEnabled(enabled);
			save(sensor);
		});
	}

	public void postNoteBatch(SsrStpGetIn input, BatchIn batchin, NoteIn body) {
		runBatch(input, batchin, item -> NoteUtils.post(PageType.COUNTER_SETTING, item.getId(), body));
	}

	public void putSubscriptionTermBatch(SsrStpGetIn input, BatchIn batchin, SsrStpPutSubscriptionTermIn body) {
		ValueUtils.assertNotEmpty(body, "subscriptionTerm");
		ValueUtils.assertNotEmpty(body.getSubscriptionTerm(), "subscriptionTerm");

		runBatch(input, batchin, item -> {
			Counter sensor = findById(item.getId());
			sensor.setSubscriptionTerm(body.getSubscriptionTerm());
			save(sensor);
		});
	}

	private void runBatch(SsrStpGetIn input, BatchIn batchin, Closure1ParamNoReturn<SsrStpItem> closure) {
		DataUtils.runBatch(Sort.unsorted(), 100, true, //
				pageable -> BeanUtils.get(SsrStpRepository.class).findAll(input, batchin, pageable), //
				item -> TranUtils.doNewTran(() -> closure.execute(item))//
		);
	}

	public void deleteTabItemsBatch(SsrStpGetIn input, BatchIn batchin) {
		LogicUtils.assertNotNull(input.getTabName(), "tabName");
		TabTable fromTab = QueryUtils.findTabTable(ObjectType.COUNTER, input.getTabName());
		runContentBatch(input, batchin, itemIds -> TabUtils.deleteAllData(itemIds, fromTab), false);
	}

	private void runContentBatch(SsrStpGetIn input, BatchIn batchin, Closure1ParamNoReturn<List<Long>> closure, boolean pageUpRequired) {
		DataUtils.runContentBatch(Sort.unsorted(), 100, pageUpRequired, //
				pageable -> BeanUtils.get(SsrStpRepository.class).findAll(input, batchin, pageable), //
				items -> TranUtils.doNewTran(() -> closure.execute(items.stream().map(ob -> ob.getId()).collect(Collectors.toList())))//
		);
	}

	public void moveTabItemsBatch(SsrStpGetIn input, BatchIn batchin, String toTabName) {
		LogicUtils.assertNotNull(input.getTabName(), "tabName");
		TabTable fromTab = QueryUtils.findTabTable(ObjectType.COUNTER, input.getTabName());
		TabTable toTab = QueryUtils.findTabTable(ObjectType.COUNTER, toTabName);
		if (toTab == null) {
			return;
		}
		runContentBatch(input, batchin, itemIds -> TabUtils.moveAllData(itemIds, fromTab, toTab), false);
	}

}
