package com.emoldino.api.production.resource.composite.alrmchdtm.service;

import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.base.note.util.NoteUtils;
import com.emoldino.api.common.resource.base.object.util.EmObjectUtils;
import com.emoldino.api.common.resource.composite.parstp.dto.ParStpItem;
import com.emoldino.api.production.resource.composite.alrmchdtm.repository.AlrMchDtmRepository;
import com.emoldino.api.production.resource.composite.alrmchdtm.dto.AlrMchDtmItem;
import com.emoldino.api.production.resource.composite.alrmchdtm.dto.AlrMchDtmGetIn;
import com.emoldino.api.production.resource.composite.alrmchdtm.dto.AlrMchDtmGetOut;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.Tab;
import com.emoldino.framework.enumeration.AlertTab;
import com.emoldino.framework.util.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import saleson.api.machineDowntimeAlert.MachineDowntimeReasonRepository;
import saleson.api.machineDowntimeAlert.payload.MachineDowntimeAlertData;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.PageType;
import saleson.model.MachineDowntimeReason;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AlrMchDtmService {

	@Transactional
	public AlrMchDtmGetOut get(AlrMchDtmGetIn input, Pageable pageable) {
		Page<AlrMchDtmItem> page = BeanUtils.get(AlrMchDtmRepository.class).findAll(input, null, pageable);
		loadMachineDowntimeReasons(page);
		List<Tab> tabs = getTabs(input, page);
		return new AlrMchDtmGetOut(page, tabs);
	}

	private void loadMachineDowntimeReasons(Page<AlrMchDtmItem> page) {
		List<Long> machineDowntimeAlertIdList = page.getContent().stream().map(AlrMchDtmItem::getId).collect(Collectors.toList());
		List<AlrMchDtmItem.MchDtmReason> machineDowntimeReasonList = BeanUtils.get(AlrMchDtmRepository.class).getMchDtmReasonList(machineDowntimeAlertIdList);
		Map<Long, List<AlrMchDtmItem.MchDtmReason>> machineDowntimeReasonMap = machineDowntimeReasonList.stream()
				.collect(Collectors.groupingBy(AlrMchDtmItem.MchDtmReason::getMachineDowntimeAlertId));
		page.getContent().stream().forEach(alrMchDtmItem -> {
			List<AlrMchDtmItem.MchDtmReason> machineDowntimeReasonItemList = machineDowntimeReasonMap.get(alrMchDtmItem.getId());
			if (CollectionUtils.isNotEmpty(machineDowntimeReasonItemList)) {
				alrMchDtmItem.setDowntimeReasonList(machineDowntimeReasonItemList);
			}
		});
	}

	private static List<Tab> getTabs(AlrMchDtmGetIn input, Page<?> page) {
		return Arrays.asList(//
				getTab(input, page, AlertTab.ALERT.getTitle()), //
				getTab(input, page, AlertTab.HISTORY_LOG.getTitle())//
		);
	}

	private static Tab getTab(AlrMchDtmGetIn input, Page<?> page, String tabName) {
		if (tabName.equals(input.getTabName())) {
			return new Tab(tabName, page.getTotalElements(), true);
		}

		AlrMchDtmGetIn countin = ValueUtils.map2(input, AlrMchDtmGetIn.class);
		countin.setTabName(tabName);
		long count = BeanUtils.get(AlrMchDtmRepository.class).count(countin);
		return new Tab(tabName, count, true);
	}

	public void postNoteBatch(AlrMchDtmGetIn input, BatchIn batchin, NoteIn body) {
		runBatch(input, batchin, item -> NoteUtils.post(PageType.MACHINE_DOWNTIME_ALERT, item.getMachineId(), body));
	}

	public void runBatch(AlrMchDtmGetIn input, BatchIn batchin, Closure1ParamNoReturn<AlrMchDtmItem> closure) {
		DataUtils.runBatch(Sort.unsorted(), 100, true, //
				pageable -> BeanUtils.get(AlrMchDtmRepository.class).findAll(input, batchin, pageable), //
				item -> TranUtils.doNewTran(() -> closure.execute(item))//
		);
	}

}
