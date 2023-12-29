package vn.com.twendie.avis.api.service.impl;

import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.core.util.StrUtils;
import vn.com.twendie.avis.api.repository.JourneyDiaryDailyCostTypeRepo;
import vn.com.twendie.avis.api.repository.JourneyDiaryStationFeeRepo;
import vn.com.twendie.avis.api.service.CostTypeService;
import vn.com.twendie.avis.api.service.JourneyDiaryDailyCostTypeService;
import vn.com.twendie.avis.data.enumtype.JourneyDiaryCostTypeEnum;
import vn.com.twendie.avis.data.model.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class JourneyDiaryDailyCostTypeServiceImpl implements JourneyDiaryDailyCostTypeService {

    private final JourneyDiaryDailyCostTypeRepo journeyDiaryDailyCostTypeRepo;
    private final JourneyDiaryStationFeeRepo journeyDiaryStationFeeRepo;
    private final CostTypeService costTypeService;

    private final StrUtils strUtils;
    private final DateUtils dateUtils;

    public JourneyDiaryDailyCostTypeServiceImpl(JourneyDiaryDailyCostTypeRepo journeyDiaryDailyCostTypeRepo,
                                                JourneyDiaryStationFeeRepo journeyDiaryStationFeeRepo,
                                                CostTypeService costTypeService,
                                                StrUtils strUtils,
                                                DateUtils dateUtils) {
        this.journeyDiaryDailyCostTypeRepo = journeyDiaryDailyCostTypeRepo;
        this.journeyDiaryStationFeeRepo = journeyDiaryStationFeeRepo;
        this.costTypeService = costTypeService;
        this.strUtils = strUtils;
        this.dateUtils = dateUtils;
    }

    @Override
    public List<JourneyDiaryDailyCostType> saveAll(Collection<JourneyDiaryDailyCostType> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        } else {
            return journeyDiaryDailyCostTypeRepo.saveAll(list);
        }
    }

    @Override
    public List<JourneyDiaryDailyCostType> createJourneyDiaryDailyCostTypes(JourneyDiaryDaily journeyDiaryDaily) {
        JourneyDiary journeyDiary = journeyDiaryDaily.getJourneyDiary();
        List<CostType> costTypes = costTypeService.findAll()
                .stream()
                .filter(costType -> JourneyDiaryCostTypeEnum.codes().contains(costType.getCode()))
                .collect(Collectors.toList());
        if (Objects.nonNull(journeyDiary) && journeyDiaryDaily.getDate().equals(dateUtils.startOfDay(journeyDiary.getTimeStart()))) {
            return costTypes.stream()
                    .map(costType -> {
                        List<JourneyDiaryCostType> journeyDiaryCostTypes = journeyDiary.getJourneyDiaryCostTypes()
                                .stream()
                                .filter(jdCostType -> jdCostType.getCostType().equals(costType))
                                .collect(Collectors.toList());
                        BigDecimal value = journeyDiaryCostTypes.stream()
                                .map(JourneyDiaryCostType::getValue)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal::add)
                                .orElse(null);
                        String imageCostLinks = journeyDiaryCostTypes.stream()
                                .map(JourneyDiaryCostType::getImageCostLink)
                                .map(strUtils::getFileName)
                                .filter(Objects::nonNull)
                                .findFirst()
                                .orElse(null);
                        return JourneyDiaryDailyCostType.builder()
                                .journeyDiaryDaily(journeyDiaryDaily)
                                .costType(costType)
                                .value(value)
                                .imageCostLink(imageCostLinks)
                                .build();
                    })
                    .collect(Collectors.toList());
        } else {
            return costTypes.stream()
                    .map(costType -> JourneyDiaryDailyCostType.builder()
                            .journeyDiaryDaily(journeyDiaryDaily)
                            .costType(costType)
                            .build())
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void fetchJourneyDiaryDailyCostTypes(Collection<JourneyDiaryDaily> journeyDiaryDailies) {
        if (!CollectionUtils.isEmpty(journeyDiaryDailies)) {
            Set<Long> ids = journeyDiaryDailies.stream()
                    .flatMap(journeyDiaryDaily ->
                            Stream.concat(Stream.of(journeyDiaryDaily), journeyDiaryDaily.getChildren().stream()))
                    .map(JourneyDiaryDaily::getId).collect(Collectors.toSet());
            List<JourneyDiaryDailyCostType> journeyDiaryDailyCostTypes = journeyDiaryDailyCostTypeRepo
                    .findByJourneyDiaryDailyIdInAndDeletedFalse(ids);
            journeyDiaryDailies.stream()
                    .flatMap(journeyDiaryDaily ->
                            Stream.concat(Stream.of(journeyDiaryDaily), journeyDiaryDaily.getChildren().stream()))
                    .forEach(journeyDiaryDaily -> journeyDiaryDaily.setJourneyDiaryDailyCostTypes(
                            journeyDiaryDailyCostTypes.stream()
                                    .filter(journeyDiaryDailyCostType -> journeyDiaryDaily.getId()
                                            .equals(journeyDiaryDailyCostType.getJourneyDiaryDailyId()))
                                    .collect(Collectors.toSet())
                    ));
        }
    }

    @Override
    public void fetchJourneyDiaryDailyStationFees(Collection<JourneyDiaryDaily> journeyDiaryDailies) {
        if (!CollectionUtils.isEmpty(journeyDiaryDailies)) {
            Set<Long> journeyDiaryIds = journeyDiaryDailies.stream()
                    .flatMap(journeyDiaryDaily ->
                            Stream.concat(Stream.of(journeyDiaryDaily), journeyDiaryDaily.getChildren().stream()))
                    .map(JourneyDiaryDaily::getJourneyDiaryId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            List<JourneyDiaryStationFee> journeyDiaryStationFees = journeyDiaryStationFeeRepo
                    .findByJourneyDiaryIdInAndDeletedFalse(journeyDiaryIds);
            Map<Long, Set<JourneyDiaryStationFee>> journeyDiaryStationFeesMap = journeyDiaryStationFees.stream()
                    .collect(Collectors.groupingBy(JourneyDiaryStationFee::getJourneyDiaryId, Collectors.toSet()));
            journeyDiaryDailies.stream()
                    .flatMap(journeyDiaryDaily ->
                            Stream.concat(Stream.of(journeyDiaryDaily), journeyDiaryDaily.getChildren().stream()))
                    .filter(journeyDiaryDaily -> Objects.nonNull(journeyDiaryDaily.getJourneyDiaryId()))
                    .filter(journeyDiaryDaily -> !journeyDiaryDaily.getFlagMultiDate() ||
                            Objects.nonNull(journeyDiaryDaily.getWorkingTimeAppFrom()))
                    .forEach(journeyDiaryDaily -> journeyDiaryDaily.setJourneyDiaryStationFees(
                            journeyDiaryStationFeesMap.getOrDefault(journeyDiaryDaily.getJourneyDiaryId(), Sets.newHashSet())
                    ));
        }
    }
}
