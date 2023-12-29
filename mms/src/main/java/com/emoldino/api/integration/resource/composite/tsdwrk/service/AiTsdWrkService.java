package com.emoldino.api.integration.resource.composite.tsdwrk.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.integration.resource.base.ai.dto.AiModelType;
import com.emoldino.api.integration.resource.base.ai.dto.AiStatistics;
import com.emoldino.api.integration.resource.base.ai.repository.aimoldfeature.AiMoldFeature;
import com.emoldino.api.integration.resource.base.ai.repository.aimoldfeature.AiMoldFeatureRepository;
import com.emoldino.api.integration.resource.base.ai.repository.aitsdresult.AiTsdResult;
import com.emoldino.api.integration.resource.base.ai.repository.aitsdresult.AiTsdResultRepository;
import com.emoldino.api.integration.resource.base.ai.repository.aitsdresult.QAiTsdResult;
import com.emoldino.api.integration.resource.composite.tsdwrk.dto.AiTsdFetchFields;
import com.emoldino.api.integration.resource.composite.tsdwrk.dto.AiTsdFetchResult;
import com.emoldino.api.integration.resource.composite.tsdwrk.dto.AiTsdResultFields;
import com.emoldino.api.integration.resource.composite.tsdwrk.dto.AiTsdResultIn;
import com.emoldino.api.integration.resource.composite.tsdwrk.dto.TsdLabel;
import com.emoldino.api.integration.util.AiUtils;
import com.emoldino.api.integration.util.KafkaMessageUtils;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.Closure1ParamNoReturn;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.SyncCtrlUtils;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AiTsdWrkService {

    private final JPAQueryFactory queryFactory;
    private final AiTsdResultRepository tsdRepo;

    public void procFirstLaunchTSD(Closure1ParamNoReturn<Instant> closure) {
        Instant lastTff = AiUtils.getLastTff();
        String firstTff = BeanUtils.get(JPAQueryFactory.class)//
                .select(Q.statistics.tff) //
                .from(Q.statistics) //
                .where(Q.statistics.tff.isNotNull()) //
                .orderBy(Q.statistics.tff.asc())//
                .fetchFirst();
        do {
            closure.execute(lastTff);
        } while (Long.valueOf(firstTff) < Long.valueOf(DateUtils2.format((lastTff = lastTff.minus(1, ChronoUnit.DAYS)), DatePattern.yyyyMMddHHmmss, Zone.SYS))
                || 20171001000000L < Long.valueOf(DateUtils2.format((lastTff = lastTff.minus(1, ChronoUnit.DAYS)), DatePattern.yyyyMMddHHmmss, Zone.SYS)));

    }

    public void procLaunchTSD(String aiFetchTopic, String mmsTopic, Instant fromTime, Instant toTime) {
		AiUtils.procFetch(mold -> {
            AiMoldFeature mfe = BeanUtils.get(AiMoldFeatureRepository.class).findByMoldId(mold.getId());
            if (ObjectUtils.isEmpty(mfe)) {
                return;
            }

			QAiTsdResult tsdResult = QAiTsdResult.aiTsdResult;
            AiTsdResult tsdLastResult = queryFactory //
                    .selectFrom(tsdResult) //
                    .where(tsdResult.moldId.eq(mold.getId())) //
                    .orderBy(tsdResult.updatedAt.desc()) //
                    .limit(1) //
                    .fetchOne();
            TsdLabel tsdLabel = null;
            if (!ObjectUtils.isEmpty(tsdLastResult)) {
                tsdLabel = tsdLastResult.getTsdLabel();
            }

			List<AiStatistics> statistics = AiUtils.getAiStatistics(mold.getId(), fromTime, toTime);
            if (!ObjectUtils.isEmpty(statistics)) {
                List<Long> statisticsId = new ArrayList<>();
                List<String> tff = new ArrayList<>();
                List<String> hour = new ArrayList<>();
                List<Integer> shotCount = new ArrayList<>();
                List<Integer> cycleTime = new ArrayList<>();
                List<Integer> tav = new ArrayList<>();

                statistics.forEach(data -> {
                    statisticsId.add(data.getId());
                    tff.add(data.getTff());
                    hour.add(data.getHour());
                    shotCount.add(data.getShotCountCtt());
                    cycleTime.add(data.getCt().intValue());
                    tav.add(data.getTav());
                });

                AiTsdFetchFields fields = AiTsdFetchFields.builder() //
                        .moldId(mold.getId()) //
                        .statisticsId(statisticsId) //
                        .tff(tff) //
                        .hour(hour) //
                        .shotCount(shotCount) //
                        .cycleTime(cycleTime) //
                        .tav(tav) //
                        .startDate(DateUtils2.format(toTime, DatePattern.yyyyMMdd, Zone.GMT)) //
                        .lastLabel(tsdLabel) //
                        .sc_min(mfe.getScMin()) //
                        .sc_max(mfe.getScMax()) //
                        .sc_5_perc(mfe.getScPerc5()) //
                        .sc_10_perc(mfe.getScPerc10()) //
                        .sc_15_perc(mfe.getScPerc15()) //
                        .sc_20_perc(mfe.getScPerc20()) //
                        .sc_25_perc(mfe.getScPerc25()) //
                        .sc_50_perc(mfe.getScPerc50()) //
                        .sc_75_perc(mfe.getScPerc75()) //
                        .build();

                AiTsdFetchResult fetchData = AiTsdFetchResult.builder()//
                        .aiType(AiModelType.AI_TSD)//
                        .data(fields)//
                        .build();

                KafkaMessageUtils.sendMessage("mms2ai_tsd_fetch", fetchData, aiFetchTopic, mmsTopic);
            }
        });
    }

    @Transactional
    public void saveTsdResult(AiTsdResultIn input) {
        AiTsdResultFields data = input.getData();
        SyncCtrlUtils.wrap("aihub.AiTsdWrkService.saveTsdResult" + data.getMoldId(), () -> {
            // Re-Labeling
            if (data.getNeedsRelabelling() == 1) {
                List<AiTsdResult> prevResults = BeanUtils.get(AiTsdResultRepository.class).findByMoldId(data.getMoldId());
                List<AiTsdResult> reLblResults = new ArrayList<>();

                prevResults.forEach(prevResult -> {
                    if (prevResult.getTsdLabel().equals(TsdLabel.DOWNTIME)) {
                        AiTsdResult result = prevResult;
                        result.setTsdLabel(data.getNewLabel());
                        reLblResults.add(result);
                    }
                });

                if (!ObjectUtils.isEmpty(reLblResults)) {
                    tsdRepo.saveAll(reLblResults);
                }
            }

            for (int i = 0; i < data.getStatisticsId().size(); i++) {
                AiTsdResult oldTsdResult = BeanUtils.get(AiTsdResultRepository.class).findByStatisticsId(data.getStatisticsId().get(i));

                AiTsdResult tsdResult = AiTsdResult.builder() //
                        .moldId(data.getMoldId()) //
                        .lowScThreshold(data.getLow_sc_threshold()) //
                        .highScThreshold(data.getHigh_sc_threshold()) //
                        .statisticsId(data.getStatisticsId().get(i)) //
                        .hour(data.getHour().get(i)) //
                        .shotCount(data.getShotCount().get(i)) //
                        .correctedShotCount(data.getCorrectedShotCount().get(i)) //
                        .tsdLabel(data.getTsdLabel().get(i)) //
                        .isMissing(data.getIsMissing().get(i)) //
                        .build();

                if (!ObjectUtils.isEmpty(oldTsdResult)) {
                    tsdResult.setId(oldTsdResult.getId());
                }

                tsdRepo.saveAndFlush(tsdResult);
            }

        });
    }
}
