package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.data.model.JourneyDiarySignature;

public interface JourneyDiarySignatureService {

//    JourneyDiarySignature findByJourneyDiary(JourneyDiary journeyDiary);

    JourneyDiarySignature findByJourneyDiaryId(Long journeyDiaryId);
}
