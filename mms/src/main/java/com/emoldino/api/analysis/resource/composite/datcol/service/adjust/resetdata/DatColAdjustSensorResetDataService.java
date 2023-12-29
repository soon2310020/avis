package com.emoldino.api.analysis.resource.composite.datcol.service.adjust.resetdata;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.base.command.dto.DeviceCommandDto;
import com.emoldino.api.analysis.resource.base.command.repository.devicecommand.DeviceCommand;
import com.emoldino.api.analysis.resource.base.command.repository.devicecommand.DeviceCommandRepository;
import com.emoldino.api.analysis.resource.base.command.repository.devicecommand.QDeviceCommand;
import com.emoldino.api.analysis.resource.base.command.service.device.DeviceCommandService;
import com.emoldino.api.analysis.resource.base.data.repository.data3collected.Data3Collected;
import com.emoldino.api.analysis.resource.base.data.repository.data3collected.Data3CollectedRepository;
import com.emoldino.api.analysis.resource.base.data.repository.data3collected.QData3Collected;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColSenRstInfo;
import com.emoldino.api.analysis.resource.composite.datcol.util.DatColUtils;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.CacheDataUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.TranUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import saleson.api.counter.CounterRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.statistics.StatisticsRepository;
import saleson.model.Counter;
import saleson.model.Mold;
import saleson.model.Statistics;

@RequiredArgsConstructor
@Service
public class DatColAdjustSensorResetDataService {

	private final JPAQueryFactory queryFactory;

	public void adjustBatch() {
		JobUtils.runIfNotRunning("DatColAdjustSensorReset", new JobOptions().setClustered(true), () -> {
			findAndAdjustBatch();
		});
	}

	// 2023.07.31 Mickey Park
	public void findAndAdjustBatch() {
		if (!TranUtils.doNewTran(() -> CacheDataUtils.exists("DatColAdjustSensorResetDataService", "adjustSensorResetData"))) {
			List<DatColSenRstInfo> adjustInfo = queryFactory //
					.select(Projections.constructor( //
							DatColSenRstInfo.class, //
							Q.mold.id.as("moldId"), //
							Q.mold.equipmentCode.as("moldCode"), //
							Q.counter.id.as("counterId"), //
							Q.counter.equipmentCode.as("counterCode"), //						
							Q.mold.lastShot.as("moldShotCount"), //
							Q.counter.shotCount.as("counterShotCount")))
					.from(Q.mold) //
					.innerJoin(Q.counter).on(Q.mold.counterId.eq(Q.counter.id)) //
					.where(Q.mold.lastShot.lt(Q.counter.shotCount)) //
					.fetch();

			if (ObjectUtils.isEmpty(adjustInfo)) {
				return;
			}

			adjustInfo.forEach(data -> {
				TranUtils.doNewTran(() -> {

					// 1. Query sn is 0
					Page<Data3Collected> page = BeanUtils.get(Data3CollectedRepository.class).findAll(//
							new BooleanBuilder()//
									.and(QData3Collected.data3Collected.deviceId.eq(data.getCounterCode())) //
									.and(QData3Collected.data3Collected.dataType.eq("DATA")) //
									.and(QData3Collected.data3Collected.sn.eq(0)), //
							PageRequest.of(0, 5, Direction.DESC, "id"));

					// 2. Get maxTff in Statistics
					Pair<String, Integer> maxTffAndSc = DatColUtils.getMaxTffAndMaxShotCount(data.getCounterCode(), data.getMoldCode());
					String maxTff = maxTffAndSc.getFirst();

					// 3. Adjust Total ShotCount
					int adjustShotCount = 0;
					DeviceCommand resetCommand = DatColUtils.getLastResetCommand(data.getCounterCode());
					if (page.getContent().size() >= 2 && resetCommand != null) {
						adjustShotCount += Integer.valueOf(resetCommand.getData());
					}
					adjustShotCount += DatColUtils.calTotalShotCount(data.getCounterCode(), resetCommand);

					// 4. Adjust Mold, Counter, Statistics
					Mold mold = BeanUtils.get(MoldRepository.class).findById(data.getMoldId()).orElse(null);
					if (mold != null) {
						mold.setLastShot(adjustShotCount);
						BeanUtils.get(MoldRepository.class).saveAndFlush(mold);
					}

					Counter counter = BeanUtils.get(CounterRepository.class).findById(data.getCounterId()).orElse(null);
					if (counter != null) {
						counter.setShotCount(adjustShotCount);
						BeanUtils.get(CounterRepository.class).saveAndFlush(counter);
					}

					// 5. Adjust the Total Shot Count of Statistics with the latest TFF
					List<Statistics> maxTffStatisticsList = queryFactory//
							.selectFrom(Q.statistics) //
							.where(Q.statistics.ci.eq(data.getCounterCode()) //
									.and(Q.statistics.moldCode.eq(data.getMoldCode())) //
									.and(Q.statistics.tff.eq(maxTff))) //									
							.fetch();

					for (Statistics st : maxTffStatisticsList) {
						st.setSc(adjustShotCount);
						BeanUtils.get(StatisticsRepository.class).saveAndFlush(st);
					}

					// 6. Create Reset Command
					postResetCommand(data.getCounterCode(), adjustShotCount);

				});
			});
		}
	}

	private static void postResetCommand(String deviceId, int shotCount) {
		String deviceType = "SS3";
		String cmd = "1";

		// Check Command Whether Already Post or not
		QDeviceCommand table = QDeviceCommand.deviceCommand;
		if (BeanUtils.get(DeviceCommandRepository.class).exists(new BooleanBuilder()//
				.and(table.deviceType.eq(deviceType))//
				.and(table.deviceId.eq(deviceId))//
				.and(table.command.eq(cmd))//
				.and(table.createdAt.goe(DateUtils2.newInstant().minusSeconds(3600))))) {
			return;
		}

		// Post OTA Command
		DeviceCommandDto command = new DeviceCommandDto();
		command.setDeviceType(deviceType);
		command.setDeviceId(deviceId);
		command.setCommand(cmd);
		command.setComment("RESET");
		command.setData(Integer.toString(shotCount));
		BeanUtils.get(DeviceCommandService.class).post(command);

	}
}
