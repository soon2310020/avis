package vn.com.twendie.avis.mobile.api.service;

import vn.com.twendie.avis.data.model.JourneyDiaryDaily;

import java.util.List;
import java.util.Set;

public interface JourneyDiaryDailyService {

    List<JourneyDiaryDaily> findByJourneyDiaryIdOrderByCreateAtAsc(Long journeyDiaryId);

    Set<String> findTripItineraryByJourneyDiaryId(Long journeyDiaryId);
}
