package vn.com.twendie.avis.mobile.api.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.data.model.JourneyDiarySignature;
import vn.com.twendie.avis.data.model.MemberCustomer;
import vn.com.twendie.avis.mobile.api.repository.JourneyDiarySignatureRepo;
import vn.com.twendie.avis.mobile.api.service.JourneyDiaryService;
import vn.com.twendie.avis.mobile.api.service.JourneyDiarySignatureService;

import java.sql.Timestamp;
import java.util.List;

@Service
public class JourneyDiarySignatureServiceImpl implements JourneyDiarySignatureService {

    private JourneyDiarySignatureRepo journeyDiarySignatureRepo;

    private JourneyDiaryService journeyDiaryService;

    public JourneyDiarySignatureServiceImpl(JourneyDiarySignatureRepo journeyDiarySignatureRepo,
                                            JourneyDiaryService journeyDiaryService){
        this.journeyDiarySignatureRepo = journeyDiarySignatureRepo;
        this.journeyDiaryService = journeyDiaryService;
    }

    @Override
    public JourneyDiarySignature save(JourneyDiarySignature journeyDiarySignature) {
        return journeyDiarySignatureRepo.save(journeyDiarySignature);
    }

    @Override
    public List<JourneyDiarySignature> saveAll(List<JourneyDiarySignature> journeyDiarySignature) {
        return journeyDiarySignatureRepo.saveAll(journeyDiarySignature);
    }

    @Override
    public Page<JourneyDiarySignature> findByMemberCustomerIdAndTimeEndBetweenOrderByCreatedAtDesc(
            long memberCustomerId, Timestamp fromDate, Timestamp toDate, int page, int size) {
        return journeyDiarySignatureRepo.findByMemberCustomerIdAndTimeEndBetweenOrderByCreatedAtDesc(memberCustomerId, fromDate, toDate, PageRequest.of(page, size));
    }

    @Override
    public List<JourneyDiarySignature> findByIdIn(List<Long> ids) {
        return journeyDiarySignatureRepo.findByIdIn(ids);
    }

    @Override
    public List<JourneyDiarySignature> findByMemberCustomerAndTimeEndBetween(MemberCustomer memberCustomer, Timestamp startTime, Timestamp endTime) {
        return findByMemberCustomerIdAndTimeEndBetweenOrderByCreatedAtDesc(memberCustomer.getId(), startTime, endTime, 0, 9999).getContent();
    }

    @Override
    public JourneyDiarySignature findByJourneyDiary(JourneyDiary journeyDiary) {
        return journeyDiarySignatureRepo.findFirstByJourneyDiary(journeyDiary);
    }

    @Override
    public JourneyDiarySignature findById(Long id) {
        return journeyDiarySignatureRepo.findById(id).orElseThrow(NotFoundException::new);
    }

    @Override
    public void migrateTimeStartAndTimeEnd() {
        List<JourneyDiarySignature> journeyDiarySignatures = journeyDiarySignatureRepo.findAll();
        journeyDiarySignatures.forEach(j ->{
            j.setTimeStart(j.getJourneyDiary() != null ? j.getJourneyDiary().getTimeStart() : null);
            j.setTimeEnd(j.getJourneyDiary() != null ? j.getJourneyDiary().getTimeEnd() : null);
        });

        journeyDiarySignatureRepo.saveAll(journeyDiarySignatures);

    }


}
