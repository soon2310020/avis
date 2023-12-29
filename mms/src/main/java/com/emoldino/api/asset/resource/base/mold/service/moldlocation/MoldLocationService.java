package com.emoldino.api.asset.resource.base.mold.service.moldlocation;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.asset.resource.base.mold.enumeration.RelocationType;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.CacheDataUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;

import saleson.api.mold.MoldLocationRepository;
import saleson.common.enumeration.MoldLocationStatus;
import saleson.model.MoldLocation;

@Service
public class MoldLocationService {

	@Async
	@Transactional(propagation = Propagation.NEVER)
	public void adjustMoldRelocationTypeBatch() {
		JobUtils.runIfNotRunning("adjustMoldRelocationType", new JobOptions().setClustered(true), () -> {

			if (!TranUtils.doNewTran(() -> CacheDataUtils.exists("MoldLocationService", "relocationTypeNull"))) {
				DataUtils.runContentBatch(MoldLocationRepository.class, //
						Q.moldLocation.relocationType.isNotNull()//
								.and(Q.moldLocation.relocationType.ne(RelocationType.AREA)),
						Sort.by("id"), 100, false, list -> {
							TranUtils.doNewTran(() -> {
								list.forEach(item -> item.setRelocationType(null));
								BeanUtils.get(MoldLocationRepository.class).saveAll(list);
							});
						});
				TranUtils.doNewTran(() -> CacheDataUtils.save("MoldLocationService", "relocationTypeNull", null));
			}

			DataUtils.runBatch(MoldLocationRepository.class, Q.moldLocation.relocationType.isNull(), Sort.by("notificationAt"), 100, false, moldLocation -> {
				TranUtils.doNewTran(() -> {
					MoldLocation[] prevOne = { null };
					if (moldLocation.getNotificationAt() == null) {
						moldLocation.setRelocationType(RelocationType.PLANT);
					} else {
						BeanUtils.get(MoldLocationRepository.class).findAll(//
								Q.moldLocation.moldId.eq(moldLocation.getMoldId())//
										.and(Q.moldLocation.notificationAt.lt(moldLocation.getNotificationAt())), //
								PageRequest.of(0, 1, Direction.DESC, "notificationAt")//
						).forEach(prevMoldLocation -> prevOne[0] = prevMoldLocation);
						if (prevOne[0] == null || !ValueUtils.equals(prevOne[0].getLocationId(), moldLocation.getLocationId())) {
							moldLocation.setRelocationType(RelocationType.PLANT);
						} else if (!ValueUtils.equals(prevOne[0].getAreaId(), moldLocation.getAreaId())) {
							moldLocation.setRelocationType(RelocationType.AREA);
							moldLocation.setMoldLocationStatus(MoldLocationStatus.APPROVED);
						} else {
							moldLocation.setRelocationType(RelocationType.UNKNOWN);
						}
					}
					BeanUtils.get(MoldLocationRepository.class).save(moldLocation);
				});
			});

		});
	}

}
