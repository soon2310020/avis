package vn.com.twendie.avis.api.service.impl;

import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.repository.JourneyDiarySignatureRepo;
import vn.com.twendie.avis.api.service.JourneyDiarySignatureService;
import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.data.model.JourneyDiarySignature;

import java.util.List;

@Service
public class JourneyDiarySignatureServiceImpl implements JourneyDiarySignatureService {

    private JourneyDiarySignatureRepo journeyDiarySignatureRepo;

    public JourneyDiarySignatureServiceImpl(JourneyDiarySignatureRepo journeyDiarySignatureRepo){
        this.journeyDiarySignatureRepo = journeyDiarySignatureRepo;
    }
//    @Override
//    public JourneyDiarySignature findByJourneyDiary(JourneyDiary journeyDiary) {
//        return journeyDiarySignatureRepo.findByJourneyDiary(journeyDiary);
//    }

    @Override
    public JourneyDiarySignature findByJourneyDiaryId(Long journeyDiaryId) {
        List<JourneyDiarySignature> journeyDiarySignatureList = journeyDiarySignatureRepo.findByJourneyDiary(journeyDiaryId);
        if(journeyDiarySignatureList.size() > 0) return journeyDiarySignatureList.get(0);
        return null;
    }
}
