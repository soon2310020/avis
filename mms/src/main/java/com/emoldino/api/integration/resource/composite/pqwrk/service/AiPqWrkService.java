package com.emoldino.api.integration.resource.composite.pqwrk.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.integration.resource.base.ai.dto.AiModelType;
import com.emoldino.api.integration.resource.base.ai.dto.AiStatistics;
import com.emoldino.api.integration.resource.base.ai.repository.aipqresult.AiPqResult;
import com.emoldino.api.integration.resource.base.ai.repository.aipqresult.AiPqResultRepository;
import com.emoldino.api.integration.resource.base.ai.repository.aipqresult.QAiPqResult;
import com.emoldino.api.integration.resource.composite.pqwrk.dto.AiPqFetchFields;
import com.emoldino.api.integration.resource.composite.pqwrk.dto.AiPqFetchResult;
import com.emoldino.api.integration.resource.composite.pqwrk.dto.AiPqResultIn;
import com.emoldino.api.integration.resource.composite.pqwrk.dto.AiPqStdInfo;
import com.emoldino.api.integration.util.AiUtils;
import com.emoldino.api.integration.util.KafkaMessageUtils;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.Closure1ParamNoReturn;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.Property;
import com.emoldino.framework.util.SyncCtrlUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import saleson.api.category.CategoryRepository;
import saleson.api.company.CompanyRepository;
import saleson.api.counter.CounterRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.part.PartRepository;
import saleson.api.statistics.StatisticsRepository;
import saleson.model.Mold;
import saleson.model.QMold;
import saleson.model.QMoldPart;
import saleson.model.QPart;
import saleson.model.Statistics;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiPqWrkService {

    private final JPAQueryFactory queryFactory;

    public void procLastLaunchPQ(Closure1ParamNoReturn<Instant> closure) {
        Instant lastTff = AiUtils.getLastTff();
        do {
            closure.execute(lastTff);
        } while (!"2022".equals(DateUtils2.format((lastTff = Objects.requireNonNull(lastTff).minus(1, ChronoUnit.HOURS)), DatePattern.yyyy, Zone.SYS)));
    }

    public void procLaunchPQ(String aiFetchTopic, String mmsTopic, Instant fromTime, Instant toTime) {

        // 1. Create Filter
        List<Long> counterIdList = queryFactory //
                .select(Q.counter.id) //
                .from(Q.counter) //
                .where(Q.counter.equipmentCode.notLike("CMS%") //
                        .or(Q.counter.equipmentCode.notLike("NCM%P%")) //
                        .or(Q.counter.equipmentCode.notLike("EMA%P%"))) //
                .fetch();

        BooleanBuilder filter = new BooleanBuilder();
        if (!ObjectUtils.isEmpty(counterIdList)) {
            filter.and(Q.mold.counterId.in(counterIdList));
        }

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

                // 3. Inquire FetchData
                List<AiStatistics> statistics = AiUtils.getAiStatistics(mold.getId(), fromTime, toTime);

                if (ObjectUtils.isEmpty(statistics))
                    return;

                List<Long> id = new ArrayList<>();
                List<Integer> shotCount = new ArrayList<>();
                List<Double> ct = new ArrayList<>();
                List<Integer> tav = new ArrayList<>();
                List<String> hour = new ArrayList<>();
                List<String> tff = new ArrayList<>();

                statistics.forEach(data -> {
                    id.add(data.getId());
                    shotCount.add(data.getShotCountCtt());
                    ct.add(data.getCt());
                    tav.add(data.getTav());
                    hour.add(data.getHour());
                    tff.add(data.getTff());
                });

                long startHour = tff.stream()//
                        .mapToLong(Long::parseLong) //
                        .min().orElse(Long.valueOf(tff.get(0)));

                AiPqFetchFields fields = AiPqFetchFields.builder() //
                        .moldId(mold.getId())//
                        .wact(mold.getWeightedAverageCycleTime())//
                        .startHour(Long.toString(startHour).substring(0, 10)) // YYYYMMDDHH
                        .statisticsId(id)//
                        .shotCount(shotCount)//
                        .cycleTime(ct)//
                        .temperature(tav)//
                        .hour(hour)//
                        .tff(tff)//
                        .build();

                AiPqFetchResult fetchData = AiPqFetchResult.builder() //
                        .aiType(AiModelType.AI_PQ)//
                        .generation(generation) //
                        .data(fields) //
                        .build();

                KafkaMessageUtils.sendMessage("mms2ai_pq_fetch", fetchData, aiFetchTopic, mmsTopic);
            }
        });
    }

    @Transactional
    public void savePqResult(AiPqResultIn reqIn) {
        SyncCtrlUtils.wrap("integration.AiPqWrkService.savePqResult" + reqIn.getData().getMoldId(), () -> {
            if (reqIn.getWillBeSaved() == 0) {
                return;
            }
            Assert.notNull(reqIn.getData().getStatisticsId(), "statisticsId is required");

            QAiPqResult pqResultTable = QAiPqResult.aiPqResult;
            Long moldId = reqIn.getData().getMoldId();

            AtomicInteger idxHolder = new AtomicInteger();
            reqIn.getData().getStatisticsId().forEach(statisticsId -> {
                Statistics statistics = BeanUtils.get(StatisticsRepository.class).findById(statisticsId)
                        .orElseThrow(() -> new BizException("STATISTICS_DATA_NOT_FOUND", "Statistics Data is not found", new Property("id", statisticsId)));

                int idx = idxHolder.getAndIncrement();
                AiPqResult pqResult = queryFactory.selectFrom(pqResultTable).where(pqResultTable.statisticsId.eq(statisticsId)).fetchOne();

                if (ObjectUtils.isEmpty(pqResult)) {
                    // 1. Set Standard Information Data
                    pqResult = new AiPqResult(); // Initialize
                    pqResult.setStatistics(statistics);
                    pqResult.setStatisticsId(statisticsId);
                    pqResult.setMoldId(moldId);
                    pqResult.setMold(BeanUtils.get(MoldRepository.class).findById(moldId)
                            .orElseThrow(() -> new BizException("MOLD_DATA_NOT_FOUND", "Mold Data is not found", new Property("id", moldId))));
                    pqResult.setHour(statistics.getHour());

                    AiPqStdInfo stdInfo = getAiPqStdInfo(moldId);

                    if (!ObjectUtils.isEmpty(stdInfo)) {
                        if (ObjectUtils.isEmpty(stdInfo.getCounterId())) {
                            throw new BizException("COUNTER_IS_NOT_FOUND", "Counter Data is not found", new Property("id", stdInfo.getCounterId()));
                        }

                        pqResult.setCounterId(stdInfo.getCounterId());
                        if (pqResult.getCounterId() != null) {
                            pqResult.setCounter(BeanUtils.get(CounterRepository.class).findById(stdInfo.getCounterId())
                                    .orElseThrow(() -> new BizException("COUNTER_DATA_NOT_FOUND", "Counter Data is not found", new Property("id", stdInfo.getCounterId()))));
                        }

                        pqResult.setPartId(stdInfo.getPartId());
                        if (pqResult.getPartId() != null) {
                            pqResult.setPart(BeanUtils.get(PartRepository.class).findById(stdInfo.getPartId())
                                    .orElseThrow(() -> new BizException("PART_DATA_NOT_FOUND", "Part Data is not found", new Property("id", stdInfo.getPartId()))));
                        }

                        pqResult.setCategoryId(stdInfo.getCategoryId());
                        if (pqResult.getCategoryId() != null) {
                            pqResult.setCategory(BeanUtils.get(CategoryRepository.class).findById(stdInfo.getCategoryId())
                                    .orElseThrow(() -> new BizException("CATEGORY_DATA_NOT_FOUND", "Category Data is not found", new Property("id", stdInfo.getCategoryId()))));
                        }

                        pqResult.setCompanyId(stdInfo.getCompanyId());
                        if (pqResult.getCompanyId() != null) {
                            pqResult.setCompany(BeanUtils.get(CompanyRepository.class).findById(stdInfo.getCompanyId())
                                    .orElseThrow(() -> new BizException("COMPANY_DATA_NOT_FOUND", "Company Data is not found", new Property("id", stdInfo.getCompanyId()))));
                        }

                        pqResult.setParentId(stdInfo.getParentId());
                        if (!ObjectUtils.isEmpty(stdInfo.getParentId())) {
                            pqResult.setParent(BeanUtils.get(CategoryRepository.class).findById(stdInfo.getParentId()).orElseThrow(
                                    () -> new BizException("PARENT_CATEGORY_DATA_NOT_FOUND", "Parent Category Data is not found", new Property("id", stdInfo.getParentId()))));
                        }
                    }
                }

                // 2. Set PQ Result
                pqResult.setCtStatus(reqIn.getData().getCtStatus().get(idx));
                pqResult.setTempStatus(reqIn.getData().getTempStatus().get(idx));
                pqResult.setPpaStatus(reqIn.getData().getPpaStatus().get(idx));
                pqResult.setQdStatus(reqIn.getData().getQdStatus().get(idx));
                pqResult.setPqResult("SUCCESS");
                pqResult.setPqResultTime(DateUtils2.newInstant());

                BeanUtils.get(AiPqResultRepository.class).saveAndFlush(pqResult);

            });
        });
    }

    private AiPqStdInfo getAiPqStdInfo(Long moldId) {
        return queryFactory //
                .select(Projections.constructor( //
                        AiPqStdInfo.class, //
                        QMold.mold.counterId, //
                        QMoldPart.moldPart.partId, //
                        QMold.mold.companyId, //
                        QPart.part.categoryId, //
                        QPart.part.category.parentId)) //
                .from(QMold.mold) //
                .innerJoin(QMoldPart.moldPart).on(QMold.mold.id.eq(QMoldPart.moldPart.moldId)) //
                .innerJoin(QPart.part).on(QPart.part.id.eq(QMoldPart.moldPart.partId)) //
                .where(QMoldPart.moldPart.moldId.eq(moldId)) //
                .orderBy(QMoldPart.moldPart.switchedTime.desc()).limit(1).fetchOne();
    }

}
