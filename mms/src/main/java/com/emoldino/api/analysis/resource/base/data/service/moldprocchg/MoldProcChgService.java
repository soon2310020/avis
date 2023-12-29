package com.emoldino.api.analysis.resource.base.data.service.moldprocchg;

import java.util.Objects;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.DataAcceleration;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.DataAccelerationRepository;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.QDataAcceleration;
import com.emoldino.api.analysis.resource.base.data.repository.moldprocchg.MoldProcChg;
import com.emoldino.api.analysis.resource.base.data.repository.moldprocchg.MoldProcChgRepository;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import lombok.RequiredArgsConstructor;
import saleson.model.Cdata;
import saleson.model.Transfer;
import saleson.service.transfer.CdataRepository;
import saleson.service.transfer.TransferRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class MoldProcChgService {
	private final DataAccelerationRepository dataAccRepo;
	private final MoldProcChgRepository repo;

	private static final QDataAcceleration qDataAcceleration = QDataAcceleration.dataAcceleration;

	public void summarizeBatch() {
		JobUtils.runIfNotRunning("sc.moldProcChg.summarizeBatch", new JobOptions().setClustered(true), () -> {
			DataUtils.runBatch(DataAccelerationRepository.class, //
					new BooleanBuilder().and(qDataAcceleration.procStatus.eq("UPDATED")), //
					Sort.by("id"), 1, false, //
					item -> {
						try {
							TranUtils.doNewTran(() -> {
								DataAcceleration dataAcc = dataAccRepo.findWithLockById(item.getId()).orElse(null);
								if (!"UPDATED".equals(Objects.requireNonNull(dataAcc).getProcStatus())) {
									return;
								}

								if (dataAcc.getMoldId() == null) {
									Transfer trs = BeanUtils.get(TransferRepository.class)//
											.findFirstByCiAndSnOrderByTffDesc(dataAcc.getCounterId(), dataAcc.getDataId())//
											.orElse(null);
									if (trs == null) {
										dataAcc.setProcStatus("SKIPPED");
										dataAccRepo.save(dataAcc);
										return;
									}

									Cdata cdata = BeanUtils.get(CdataRepository.class).findById(trs.getId()).orElse(null);
									if (cdata == null || cdata.getMoldId() == null) {
										dataAcc.setProcStatus("SKIPPED");
										dataAccRepo.save(dataAcc);
										return;
									}

									dataAcc.setMoldId(cdata.getMoldId());
								}

								summarize(dataAcc);

								dataAcc.setProcStatus("APPLIED");
								dataAccRepo.save(dataAcc);
							});
						} catch (Exception e) {
							AbstractException ae = ValueUtils.toAe(e, null);
							LogUtils.saveErrorQuietly(ae);

							TranUtils.doNewTran(() -> {
								DataAcceleration dataAcc = dataAccRepo.findWithLockById(item.getId()).orElse(null);
								if (!"UPDATED".equals(Objects.requireNonNull(dataAcc).getProcStatus())) {
									return;
								}
								
								dataAcc.setProcStatus("APPLY_ERROR");
								dataAcc.setProcErrorId(ae.getId());

								dataAccRepo.save(dataAcc);
							});
						}
					});
		});
	}

	private void summarize(DataAcceleration dataAcc) {
		String zoneId = LocationUtils.getZoneIdByTerminalCode(dataAcc.getTerminalId());
		String procChgTime = DateUtils2.toOtherZone(dataAcc.getMeasurementDate(), DatePattern.yyyyMMddHHmmss, Zone.GMT, zoneId);

		MoldProcChg data = repo.findOneByMoldIdAndProcChgTime(dataAcc.getMoldId(), procChgTime).orElse(null);
		if (!ValueUtils.toBoolean(dataAcc.getProcChanged(), false)) {
			if (data != null) {
				repo.delete(data);
			}
		} else if (data == null) {
			data = new MoldProcChg();
			data.setMoldId(dataAcc.getMoldId());
			data.setProcChgTime(procChgTime);
			data.setYear(ValueUtils.abbreviate(procChgTime, 4));
			data.setMonth(ValueUtils.abbreviate(procChgTime, 6));
			data.setWeek(DateUtils2.toOtherPattern(procChgTime, DatePattern.yyyyMMddHHmmss, DatePattern.YYYYww));
			data.setDay(ValueUtils.abbreviate(procChgTime, 8));
			data.setHour(ValueUtils.abbreviate(procChgTime, 10));
			repo.save(data);
		}
	}
}
