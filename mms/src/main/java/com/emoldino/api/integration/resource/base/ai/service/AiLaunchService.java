package com.emoldino.api.integration.resource.base.ai.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.emoldino.api.analysis.resource.base.data.repository.dataacceleration.QDataAcceleration;
import com.emoldino.api.integration.kafka.config.KafkaConfig;
import com.emoldino.api.integration.resource.base.ai.dto.AiFetchInferenceBoundary;
import com.emoldino.api.integration.resource.base.ai.dto.AiFetchInferenceBoundaryType;
import com.emoldino.api.integration.resource.base.ai.dto.AiFetchScaleType;
import com.emoldino.api.integration.resource.base.ai.dto.AiFetchSchema;
import com.emoldino.api.integration.resource.base.ai.dto.AiModelType;
import com.emoldino.api.integration.resource.base.ai.repository.aimoldfeature.AiMoldFeatureRepository;
import com.emoldino.api.integration.resource.base.ai.repository.aipqresult.AiPqResultRepository;
import com.emoldino.api.integration.resource.base.ai.repository.aitsdresult.AiTsdResultRepository;
import com.emoldino.api.integration.resource.composite.mfewrk.service.AiMfeWrkService;
import com.emoldino.api.integration.resource.composite.pcwrk.service.AiPcWrkService;
import com.emoldino.api.integration.resource.composite.pqwrk.service.AiPqWrkService;
import com.emoldino.api.integration.resource.composite.tsdwrk.service.AiTsdWrkService;
import com.emoldino.api.integration.util.AiUtils;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.ResourceUtils;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiLaunchService {
	private final String schemaFileDir = "/aihub/schema/";
	private final KafkaConfig kafkaConfig;

	public void launch(AiModelType aiModel) {
		JobUtils.runIfNotRunning("AiLaunchService.launch." + aiModel.name(), new JobOptions().setClustered(true), () -> {
			// 1. Get Schema 
			AiFetchSchema schema = fetchSchemaForModel(aiModel);

			// 2. Set TimeRange [timeRange[0], timeRange[1]]
			Instant[] timeRange = calculateTimeRange(Objects.requireNonNull(schema));

			// 3. Launch AI Fetch
			switch (aiModel) {
				case AI_PQ:
					launchPQ(schema, timeRange);
					break;
				case AI_MFE:
					launchMFE(timeRange);
					break;
				case AI_TSD:
					launchTSD(schema, timeRange);
					break;
				case AI_PC:
					launchPC(schema, timeRange);
					break;
				case AI_PPF:
					launchPPF(schema, timeRange);
					break;
				default:
					throw new BizException("DATA_NOT_FOUND", "AI Model:" + aiModel + " is not found!!");
			}
		});
	}

	private AiFetchSchema fetchSchemaForModel(AiModelType aiModel) {
		return ResourceUtils.toRequiredType(
				String.format("classpath:" + schemaFileDir + "%s" + ".json", aiModel.name()), AiFetchSchema.class
		);
	}

	private Instant[] calculateTimeRange(AiFetchSchema schema) {
		// Basic TimeRange
		Instant toTime = Instant.now().minus(1, ChronoUnit.DAYS);
		Instant fromTime = toTime;

		// Dynamic Scheduler & Event Processing
		int rowCnt = 0;
		String rowInquireType = "FB";
		AiFetchInferenceBoundary event = schema.getInferenceBoundary();
		AiFetchInferenceBoundaryType type = event.getType();

		if (AiFetchInferenceBoundaryType.BATCH.equals(type)) {
			// TO-DO : Dynamic Scheduler
		} else if (AiFetchInferenceBoundaryType.DATA_RECV.equals(type)) {
			// TO-DO : Event Processing
		}

		if (AiFetchScaleType.ROW.equals(schema.getScale().getType())) {
			String[] values = schema.getScale().getValue().split(",");
			rowCnt = Integer.parseInt(values[0]);
			rowInquireType = values[1]; // TO-DO
			String periodStr = values[2];
			fromTime = AiUtils.minusTime(periodStr, fromTime);
		} else if (AiFetchScaleType.PERIOD.equals(schema.getScale().getType())) {
			fromTime = AiUtils.minusTime(schema.getScale().getValue(), fromTime);
		}

		return new Instant[]{fromTime, toTime};
	}

	private void launchPQ(AiFetchSchema schema, Instant[] timeRange) {
		AiPqWrkService aiPqWrkService = BeanUtils.get(AiPqWrkService.class);

		long count = BeanUtils.get(AiPqResultRepository.class).count();
		if (count == 0) {
			// If there are no PQ results, launch all 2023 data.
			aiPqWrkService.procLastLaunchPQ(toTime -> {
                Instant fromTime = AiUtils.minusTime(schema.getScale().getValue(), toTime);
                BeanUtils.get(AiPqWrkService.class).procLaunchPQ(kafkaConfig.getAiFetchTopic(), kafkaConfig.getMmsTopic(), fromTime, toTime);
            });
		} else {
			aiPqWrkService.procLaunchPQ(kafkaConfig.getAiFetchTopic(), kafkaConfig.getMmsTopic(), timeRange[0], timeRange[1]);
		}
	}

	private void launchMFE(Instant[] timeRange) {
		BeanUtils.get(AiMfeWrkService.class).launchMFE(kafkaConfig.getAiFetchTopic(), kafkaConfig.getMmsTopic(), timeRange[0], timeRange[1]);
	}

	private void launchTSD(AiFetchSchema schema, Instant[] timeRange) {
		AiTsdWrkService aiTsdWrkService = BeanUtils.get(AiTsdWrkService.class);

		long mftCount = BeanUtils.get(AiMoldFeatureRepository.class).count();
		if (mftCount == 0) {
			BeanUtils.get(AiMfeWrkService.class).launchMFE(kafkaConfig.getAiFetchTopic(), kafkaConfig.getMmsTopic(), timeRange[0], timeRange[1]);
		} else {
			long tsdCount = BeanUtils.get(AiTsdResultRepository.class).count();
			if (tsdCount == 0) {
				// If there are no TSD results, launch all data.
				aiTsdWrkService.procFirstLaunchTSD(toTime -> {
                    Instant fromTime = AiUtils.minusTime(schema.getScale().getValue(), toTime);
                    aiTsdWrkService.procLaunchTSD(kafkaConfig.getAiFetchTopic(), kafkaConfig.getMmsTopic(), fromTime, toTime);
                });
			} else {
				aiTsdWrkService.procLaunchTSD(kafkaConfig.getAiFetchTopic(), kafkaConfig.getMmsTopic(), timeRange[0], timeRange[1]);
			}
		}
	}

	private void launchPC(AiFetchSchema schema, Instant[] timeRange) {
		QDataAcceleration table = QDataAcceleration.dataAcceleration;
		long count = BeanUtils.get(JPAQueryFactory.class)//
				.selectFrom(table)//
				.where(table.procStatus.in(Arrays.asList("UPDATED", "UPDATE_ERROR", "APPLIED")) //
						.and(table.counterId.startsWith("EMA"))) //
				.fetchCount();

		timeRange[1] = AiUtils.minusTime("6H", Instant.now());
		timeRange[0] = AiUtils.minusTime(schema.getScale().getValue(), timeRange[1]);

		AiPcWrkService aiPcWrkService = BeanUtils.get(AiPcWrkService.class);

		if (count == 0) {
			// If there are no PC results, launch all data.
			aiPcWrkService.procFirstLaunchPC(fromTime -> {
                Instant toTime = AiUtils.plusTime(schema.getScale().getValue(), fromTime);
				aiPcWrkService.procLaunchPC(kafkaConfig.getAiFetchTopic(), kafkaConfig.getMmsTopic(), fromTime, toTime);
            });
		} else {
			aiPcWrkService.procLaunchPC(kafkaConfig.getAiFetchTopic(), kafkaConfig.getMmsTopic(), timeRange[0], timeRange[1]);
		}
	}

	private void launchPPF(AiFetchSchema schema, Instant[] timeRange) {
		/*if (BeanUtils.get(AiMoldFeatureRepository.class).count() == 0) {
			BeanUtils.get(AiMfeWrkService.class).launchMFE(kafkaConfig.getAiFetchTopic(), kafkaConfig.getMmsTopic(), timeRange[0], timeRange[1]);
		}

		if (BeanUtils.get(AiTsdResultRepository.class).count() == 0) {
			AiTsdWrkService aiTsdWrkService = BeanUtils.get(AiTsdWrkService.class);
			aiTsdWrkService.procFirstLaunchTSD(toTime -> {
				Instant fromTime = AiUtils.minusTime(schema.getScale().getValue(), toTime);
				aiTsdWrkService.procLaunchTSD(kafkaConfig.getAiFetchTopic(), kafkaConfig.getMmsTopic(), fromTime, toTime);
			});
		}*/
	}

}
