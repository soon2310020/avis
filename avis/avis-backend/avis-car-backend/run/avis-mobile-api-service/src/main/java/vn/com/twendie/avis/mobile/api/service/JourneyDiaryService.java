package vn.com.twendie.avis.mobile.api.service;

import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.mobile.api.model.response.JourneyDiaryTotalInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface JourneyDiaryService {

    JourneyDiary findById(Long id);

    JourneyDiaryTotalInfo getJourneyDiaryTotalInfo(Long id);

    JourneyDiary save(JourneyDiary journeyDiary);

    default void fetchJourneyDiaryCostTypes(JourneyDiary journeyDiary) {
        fetchJourneyDiaryCostTypes(Collections.singleton(journeyDiary));
    }

    List<JourneyDiary> findByIdInAndStep(List<Long> ids, long step);

    void fetchJourneyDiaryCostTypes(Collection<JourneyDiary> journeyDiaries);

    default void fetchJourneyDiaryStationFees(JourneyDiary journeyDiary) {
        fetchJourneyDiaryStationFees(Collections.singleton(journeyDiary));
    }

    void fetchJourneyDiaryStationFees(Collection<JourneyDiary> journeyDiaries);

    void updateTotalKmGps(Long journeyDiaryId);

    void updateTotalKmGps(JourneyDiary journeyDiary) throws Exception;

    void updateJourneyDiaryStationFees(JourneyDiary journeyDiary);
}
