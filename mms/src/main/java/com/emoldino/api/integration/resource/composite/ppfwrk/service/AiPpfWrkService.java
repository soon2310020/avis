package com.emoldino.api.integration.resource.composite.ppfwrk.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.integration.resource.composite.ppfwrk.dto.AiPpfResultIn;
import com.emoldino.framework.util.Closure1ParamNoReturn;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AiPpfWrkService {

	private final JPAQueryFactory queryFactory;

	public void procFirstLaunchPPF(Closure1ParamNoReturn<Instant> closure) {

	}

	@Transactional
	public void procLaunchPPF(String aiFetchTopic, String mmsTopic, Instant fromTime, Instant toTime) {
		// 1. Create Filter
		/*List<Long> counterIdList = queryFactory //
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
						.aiType(AiModelType.AI_PPF) //
						.generation(AiUtils.getSensorGeneration(sensorIds.get(0))) //
						.data(fields) //
						.build();

				KafkaMessageUtils.sendMessage("mms2ai_ppf_fetch", fetchData, aiFetchTopic, mmsTopic);
			}
		});*/
	}

	@Transactional
	public void savePpfResult(AiPpfResultIn reqIn) {
		/*SyncCtrlUtils.wrap("integration.AiPpfWrkService.savePpfResult" + reqIn.getData().getMoldId(), () -> {

		});*/
	}
}
