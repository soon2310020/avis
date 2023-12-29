package com.emoldino.api.integration.resource.composite.pcwrk.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.DataAcceleration;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.DataAccelerationRepository;
import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.QDataAcceleration;
import com.emoldino.api.integration.resource.base.ai.dto.AiModelType;
import com.emoldino.api.integration.resource.composite.pcwrk.dto.AiPcFetchFields;
import com.emoldino.api.integration.resource.composite.pcwrk.dto.AiPcFetchResult;
import com.emoldino.api.integration.resource.composite.pcwrk.dto.AiPcResultIn;
import com.emoldino.api.integration.util.AiUtils;
import com.emoldino.api.integration.util.KafkaMessageUtils;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.Closure1ParamNoReturn;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.Property;
import com.emoldino.framework.util.SyncCtrlUtils;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import saleson.model.Mold;

@RequiredArgsConstructor
@Service
public class AiPcWrkService {

	private final JPAQueryFactory queryFactory;

	public void procFirstLaunchPC(Closure1ParamNoReturn<Instant> closure) {
		QDataAcceleration table = QDataAcceleration.dataAcceleration;

		// 1. Get First Measurement time in DATA_ACCELERATION
		String firstTime = BeanUtils.get(JPAQueryFactory.class) //
				.select(table.measurementDate) //
				.from(table) //
				.where(table.counterId.startsWith("EMA")) //
				.orderBy(table.measurementDate.asc()) //
				.fetchFirst();

		if (ObjectUtils.isEmpty(firstTime)) {
			return;
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		LocalDateTime firstTimeLdt = LocalDateTime.parse(firstTime, formatter);

		Instant firstMeasurementTime = AiUtils.resetTimeToZero(firstTimeLdt.toInstant(ZoneOffset.UTC), 3);

		// 2. Get Last Measurement time in DATA_ACCELERATION
		String lastTime = BeanUtils.get(JPAQueryFactory.class) //
				.select(table.measurementDate) //
				.from(table) //
				.where(table.counterId.startsWith("EMA")) //
				.orderBy(table.measurementDate.desc()) //
				.fetchFirst();
		LocalDateTime lastTimeLdt = LocalDateTime.parse(lastTime, formatter);
		Instant lastMeasurementTime = AiUtils.resetTimeToMax(lastTimeLdt.toInstant(ZoneOffset.UTC), 3);

		// 3. Fetch
		do {
			closure.execute(firstMeasurementTime);
		} while (Long.valueOf(DateUtils2.format((lastMeasurementTime.plus(8, ChronoUnit.HOURS)), DatePattern.yyyyMMddHHmmss, Zone.SYS)) //  
				> Long.valueOf(DateUtils2.format((firstMeasurementTime = firstMeasurementTime.plus(4, ChronoUnit.HOURS)), DatePattern.yyyyMMddHHmmss, Zone.SYS)));
	}

	@Transactional
	public void procLaunchPC(String aiFetchTopic, String mmsTopic, Instant fromTime, Instant toTime) {
		// 1. Create Filter
		List<Long> counterIdList = queryFactory //
				.select(Q.counter.id) //
				.from(Q.counter) //
				.where(Q.counter.equipmentCode.like("EMA%")) //
				.fetch();

		BooleanBuilder filter = new BooleanBuilder();
		if (!ObjectUtils.isEmpty(counterIdList)) {
			filter.and(Q.mold.counterId.in(counterIdList));
		}

		QDataAcceleration dataAcceleration = QDataAcceleration.dataAcceleration;

		AiUtils.procFetch(filter, new Closure1ParamNoReturn<Mold>() {
			@Override
			public void execute(Mold mold) {

				List<DataAcceleration> accData = queryFactory //
						.selectFrom(dataAcceleration) //
						.where(dataAcceleration.moldId.eq(mold.getId()) //
								.and(dataAcceleration.measurementDate.goe(DateUtils2.format(fromTime, DatePattern.yyyyMMddHHmmss, Zone.GMT))) //
								.and(dataAcceleration.measurementDate.loe(DateUtils2.format(toTime, DatePattern.yyyyMMddHHmmss, Zone.GMT)))) //
						.fetch();

				if (ObjectUtils.isEmpty(accData)) {
					return;
				}

				List<String> sensorIds = new ArrayList<>();
				List<Long> dataAccId = new ArrayList<>();
				List<String> measurementDate = new ArrayList<>();
				List<String> accelerations = new ArrayList<>();
				List<Double> similarityMetric = new ArrayList<>();
				List<Double> similarityMetricHr = new ArrayList<>();
				List<Integer> procChanged = new ArrayList<>();

				ObjectMapper objectMapper = new ObjectMapper();
				accData.forEach(data -> {
					sensorIds.add(data.getCounterId());
					dataAccId.add(data.getId());
					measurementDate.add(data.getMeasurementDate());
					try {
						accelerations.add(objectMapper.writeValueAsString(data.getAccelerations()));
					} catch (JsonProcessingException e) {
						accelerations.add("[]");
					}
					similarityMetric.add(data.getSimilarityMetric());
					similarityMetricHr.add(data.getSimilarityMetricHr());
					procChanged.add(data.getProcChanged() ? 1 : 0);
				});

				// 2. Get Sensor Generation		
				if (ObjectUtils.isEmpty(sensorIds)) {
					String sensorId = queryFactory. //
							select(Q.counter.equipmentCode)//
							.from(Q.counter) //
							.where(Q.counter.id.eq(mold.getCounterId())) //
							.fetchFirst();
					sensorIds.add(sensorId);
				}

				AiPcFetchFields fields = AiPcFetchFields.builder() //
						.moldId(mold.getId()) //
						.counterId(sensorIds.get(0)) //
						.contractedCycleTime(mold.getContractedCycleTime() == null ? (int) Math.round(mold.getWeightedAverageCycleTime()) : mold.getContractedCycleTime()) //
						.startHour(DateUtils2.format(fromTime, DatePattern.yyyyMMddHH, Zone.GMT)) // YYYYMMDDHH
						.dataAccId(dataAccId) //
						.measurementDate(measurementDate) //
						.accelerations(accelerations) //
						.similarityMetric(similarityMetric) //
						.similarityMetricHr(similarityMetricHr) //
						.procChanged(procChanged) //
						.build();

				AiPcFetchResult fetchData = AiPcFetchResult.builder() //
						.aiType(AiModelType.AI_PC) //
						.generation(AiUtils.getSensorGeneration(sensorIds.get(0))) //
						.data(fields) //
						.build();

				KafkaMessageUtils.sendMessage("mms2ai_pc_fetch", fetchData, aiFetchTopic, mmsTopic);

			}
		});
	}

	@Transactional
	public void savePcResult(AiPcResultIn reqIn) {
		SyncCtrlUtils.wrap("integration.AiPcWrkService.savePcResult" + reqIn.getData().getMoldId(), () -> {
			Assert.notNull(reqIn.getData().getDataAccId(), "DATA_ACCELERATION ID is required");

			AtomicInteger idxHolder = new AtomicInteger();
			reqIn.getData().getDataAccId().forEach(id -> {
				int idx = idxHolder.getAndIncrement();
				DataAcceleration accData = BeanUtils.get(DataAccelerationRepository.class).findById(id) //
						.orElseThrow(() -> new BizException("DATA_ACCELERATION_DATA_IS_NOT_FOUND", "DataAcceleration Data is not found", new Property("id", id)));

				// 1. Set Data
				accData.setMoldId(reqIn.getData().getMoldId());
				accData.setSimilarityMetric(reqIn.getData().getSimilarityMetric().get(idx));
				accData.setSimilarityMetricHr(reqIn.getData().getSimilarityMetricHr().get(idx));

				if (reqIn.getData().getProcChanged().get(idx) == 0) {
					accData.setProcChanged(false);
					accData.setProcStatus("UPDATED");
				} else if (reqIn.getData().getProcChanged().get(idx) == 1) {
					accData.setProcChanged(true);
					accData.setProcStatus("UPDATED");
				} else {
					accData.setProcChanged(null);
					accData.setProcStatus("UPDATE_ERROR");
				}

				// TO-DO : Set Error ID

				// 2. Save Data in DATA_ACCELERATION 				
				BeanUtils.get(DataAccelerationRepository.class).saveAndFlush(accData);
			});

		});
	}
}
