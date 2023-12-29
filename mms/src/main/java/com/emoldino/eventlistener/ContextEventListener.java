package com.emoldino.eventlistener;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.emoldino.api.asset.resource.base.mold.service.MoldService;
import com.emoldino.api.asset.resource.base.mold.service.moldlocation.MoldLocationService;
import com.emoldino.api.common.resource.base.client.util.ClientUtils;
import com.emoldino.api.common.resource.base.masterdata.service.MasterDataService;
import com.emoldino.framework.util.BeanUtils;

@Component
public class ContextEventListener {

	@EventListener
	public void handleContextRefreshEvent(ContextRefreshedEvent event) {

		ClientUtils.load();

//		BeanUtils.get(ExpiredDataService.class).cleanBatch();

//		BeanUtils.get(OptionService.class).migrateBatch();

//		BeanUtils.get(Data3Service.class).migrateDuplicated();

//		BeanUtils.get(DatColCorrectLeafYearDataService.class).post();

//		BeanUtils.get(DatColService.class).postShotCountCtt(new DatColPostShotCountCttIn());

//		BeanUtils.get(ProductStatService.class).adjustPartStat();

//		BeanUtils.get(CounterService.class).populateBatch();

		BeanUtils.get(MoldService.class).adjustBatch();

		BeanUtils.get(MoldLocationService.class).adjustMoldRelocationTypeBatch();

		BeanUtils.get(MasterDataService.class).adjustBatch();

//		BeanUtils.get(saleson.api.mold.MoldService.class).migratePMWorkOrderDueDate();

	}

	@EventListener
	public void handleApplicationStartedEvent(ApplicationStartedEvent event) {
//		BeanUtils.get(DatColAdjustCycleTimeDataService.class).adjustBatch();
	}

}
