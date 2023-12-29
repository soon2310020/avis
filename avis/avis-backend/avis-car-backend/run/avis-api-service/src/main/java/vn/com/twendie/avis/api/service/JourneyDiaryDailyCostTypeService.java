package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.data.model.JourneyDiaryDaily;
import vn.com.twendie.avis.data.model.JourneyDiaryDailyCostType;
import vn.com.twendie.avis.data.model.JourneyDiaryStationFee;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface JourneyDiaryDailyCostTypeService {

    List<JourneyDiaryDailyCostType> saveAll(Collection<JourneyDiaryDailyCostType> list);

    List<JourneyDiaryDailyCostType> createJourneyDiaryDailyCostTypes(JourneyDiaryDaily journeyDiaryDaily);

    default List<JourneyDiaryDailyCostType> createJourneyDiaryDailyCostTypes(Collection<JourneyDiaryDaily> journeyDiaryDailies) {
        return journeyDiaryDailies.stream()
                .map(this::createJourneyDiaryDailyCostTypes)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    void fetchJourneyDiaryDailyCostTypes(Collection<JourneyDiaryDaily> journeyDiaryDailies);

    default List<JourneyDiaryDailyCostType> fetchJourneyDiaryDailyCostTypes(JourneyDiaryDaily journeyDiaryDaily) {
        fetchJourneyDiaryDailyCostTypes(Collections.singleton(journeyDiaryDaily));
        return journeyDiaryDaily.getJourneyDiaryDailyCostTypes();
    }

    void fetchJourneyDiaryDailyStationFees(Collection<JourneyDiaryDaily> journeyDiaryDailies);

    default Set<JourneyDiaryStationFee> fetchJourneyDiaryDailyStationFees(JourneyDiaryDaily journeyDiaryDaily) {
        fetchJourneyDiaryDailyStationFees(Collections.singleton(journeyDiaryDaily));
        return journeyDiaryDaily.getJourneyDiaryStationFees();
    }

}
