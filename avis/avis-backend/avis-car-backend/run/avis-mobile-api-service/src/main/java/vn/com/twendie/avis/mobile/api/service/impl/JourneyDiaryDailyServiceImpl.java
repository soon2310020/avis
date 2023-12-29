package vn.com.twendie.avis.mobile.api.service.impl;

import org.springframework.stereotype.Service;
import vn.com.twendie.avis.data.model.JourneyDiaryDaily;
import vn.com.twendie.avis.mobile.api.repository.JourneyDiaryDailyRepo;
import vn.com.twendie.avis.mobile.api.service.JourneyDiaryDailyService;

import java.util.List;
import java.util.Set;

@Service
public class JourneyDiaryDailyServiceImpl implements JourneyDiaryDailyService {


    private JourneyDiaryDailyRepo journeyDiaryDailyRepo;

    public JourneyDiaryDailyServiceImpl (JourneyDiaryDailyRepo journeyDiaryDailyRepo){
        this.journeyDiaryDailyRepo = journeyDiaryDailyRepo;
    }

    @Override
    public List<JourneyDiaryDaily> findByJourneyDiaryIdOrderByCreateAtAsc(Long journeyDiaryDailyId) {
        return journeyDiaryDailyRepo.findByJourneyDiaryIdOrderByCreatedAtAsc(journeyDiaryDailyId);
    }

    @Override
    public Set<String> findTripItineraryByJourneyDiaryId(Long journeyDiaryId) {
        return journeyDiaryDailyRepo.findTripItineraryByJourneyDiaryId(journeyDiaryId);
    }

}
