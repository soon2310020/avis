package com.emoldino.api.integration.resource.composite.mfewrk.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.integration.resource.base.ai.dto.AiModelType;
import com.emoldino.api.integration.resource.base.ai.dto.AiStatistics;
import com.emoldino.api.integration.resource.base.ai.repository.aimoldfeature.AiMoldFeature;
import com.emoldino.api.integration.resource.base.ai.repository.aimoldfeature.AiMoldFeatureRepository;
import com.emoldino.api.integration.resource.composite.mfewrk.dto.AiMfeFetchFields;
import com.emoldino.api.integration.resource.composite.mfewrk.dto.AiMfeFetchResult;
import com.emoldino.api.integration.resource.composite.mfewrk.dto.AiMfeResultFields;
import com.emoldino.api.integration.resource.composite.mfewrk.dto.AiMfeResultIn;
import com.emoldino.api.integration.util.AiUtils;
import com.emoldino.api.integration.util.KafkaMessageUtils;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.Closure1ParamNoReturn;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.SyncCtrlUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import saleson.api.mold.MoldRepository;
import saleson.model.Mold;

@RequiredArgsConstructor
@Service
public class AiMfeWrkService {
    private final JPAQueryFactory queryFactory;
    private final AiMoldFeatureRepository amfRepo;

    public void launchMFE(String aiFetchTopic, String mmsTopic, Instant fromTime, Instant toTime) {
        // 1. Create Filter
        BooleanBuilder filter = new BooleanBuilder();
        filter.and(Q.mold.counterId.isNotNull());

        AiUtils.procFetch(filter, new Closure1ParamNoReturn<Mold>() {
            @Override
            public void execute(Mold mold) {
                // 2. Get Sensor Generation
                String sensorId = queryFactory. //
                        select(Q.counter.equipmentCode)//
                        .from(Q.counter) //
                        .where(Q.counter.id.eq(mold.getCounterId())) //
                        .fetchFirst();

                if (ObjectUtils.isEmpty(sensorId)) {
                    return;
                }

                int generation = AiUtils.getSensorGeneration(sensorId);

                List<AiStatistics> statistics = AiUtils.getAiStatistics(mold.getId(), fromTime, toTime);

                if (!ObjectUtils.isEmpty(statistics)) {
                    List<String> tff = new ArrayList<String>();
                    List<String> hour = new ArrayList<String>();
                    List<Integer> shotCount = new ArrayList<Integer>();
                    List<Integer> tav = new ArrayList<Integer>();
                    List<Integer> cycleTime = new ArrayList<Integer>();

                    statistics.forEach(data -> {
                        tff.add(data.getTff());
                        hour.add(data.getHour());
                        shotCount.add(data.getShotCountCtt());
                        tav.add(data.getTav());
                        cycleTime.add(data.getCt().intValue());
                    });

                    AiMfeFetchFields fetchFields = AiMfeFetchFields.builder() //
                            .moldId(mold.getId()) //
                            .tff(tff) //
                            .hour(hour) //
                            .shotCount(shotCount) //
                            .tav(tav) //
                            .cycleTime(cycleTime) //
                            .build();

                    AiMfeFetchResult fetchData = AiMfeFetchResult.builder() //
                            .aiType(AiModelType.AI_MFE) //
                            .generation(generation) //
                            .data(fetchFields) //
                            .build();

                    KafkaMessageUtils.sendMessage("mms2ai_mfe_fetch", fetchData, aiFetchTopic, mmsTopic);
                }
            }
        });
    }

    @Transactional
    public void saveMfeResult(AiMfeResultIn input) {
        SyncCtrlUtils.wrap("aihub.AiTsdWrkService.saveMfeResult" + input.getData().getMoldId(), () -> {

            AiMfeResultFields data = input.getData();
            AiMoldFeature mfe = AiMoldFeature.builder() //
                    .moldId(data.getMoldId()) //
                    .mold(BeanUtils.get(MoldRepository.class).findById(data.getMoldId()).orElseThrow(() -> DataUtils.newDataNotFoundException(Mold.class, "Id", data.getMoldId()))) //
                    .scMin(data.getSc_minimum()) //
                    .scPerc5(data.getSc_5_percentile()) //
                    .scPerc10(data.getSc_10_percentile()) //
                    .scPerc15(data.getSc_15_percentile()) //
                    .scPerc20(data.getSc_20_percentile()) //
                    .scPerc25(data.getSc_25_percentile()) //
                    .scPerc50(data.getSc_50_percentile()) //
                    .scPerc75(data.getSc_75_percentile()) //
                    .scMax(data.getSc_maximum()) //
                    .scMode(data.getSc_mode()) //
                    .scMean(data.getSc_mean()) //
                    .uptimeMedian(data.getUptime_mean()) //
                    .uptimeMode(data.getUptime_mode()) //
                    .uptimeCount(data.getUptime_count()) //
                    .moldClass(data.getMold_class()) //
                    .build();

            AiMoldFeature oldMfe = BeanUtils.get(AiMoldFeatureRepository.class).findByMoldId(data.getMoldId());
            if (!ObjectUtils.isEmpty(oldMfe)) {
                mfe.setId(oldMfe.getId());
            }
            amfRepo.saveAndFlush(mfe);
        });
    }

}
