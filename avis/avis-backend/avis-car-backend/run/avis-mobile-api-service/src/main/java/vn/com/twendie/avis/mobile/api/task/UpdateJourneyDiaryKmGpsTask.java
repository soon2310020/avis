package vn.com.twendie.avis.mobile.api.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.mobile.api.repository.JourneyDiaryRepo;
import vn.com.twendie.avis.mobile.api.service.JourneyDiaryService;

import java.sql.Timestamp;
import java.util.List;

@Service
@Slf4j
public class UpdateJourneyDiaryKmGpsTask {

    private final JourneyDiaryRepo journeyDiaryRepo;
    private final JourneyDiaryService journeyDiaryService;

    private final DateUtils dateUtils;

    public UpdateJourneyDiaryKmGpsTask(JourneyDiaryRepo journeyDiaryRepo,
                                       JourneyDiaryService journeyDiaryService,
                                       DateUtils dateUtils) {
        this.journeyDiaryRepo = journeyDiaryRepo;
        this.journeyDiaryService = journeyDiaryService;
        this.dateUtils = dateUtils;
    }

    @Scheduled(fixedDelay = 3600000, initialDelay = 30000)
    public void updateJourneyDiaryKmGps() {
        Timestamp from = dateUtils.startOfToday();
        Timestamp to = dateUtils.now();
        List<JourneyDiary> journeyDiaries = journeyDiaryRepo.findByTimeEndBetweenAndDeletedFalse(from, to);
        journeyDiaries.forEach(journeyDiary -> {
            try {
                journeyDiaryService.updateTotalKmGps(journeyDiary);
            } catch (Exception e) {
                log.error("Error total km gps journey_diary id {}: {}", journeyDiary.getId(), ExceptionUtils.getRootCauseMessage(e));
            }
        });
    }

}
