package vn.com.twendie.avis.mobile.api.service;

import org.springframework.data.domain.Page;
import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.data.model.JourneyDiarySignature;
import vn.com.twendie.avis.data.model.MemberCustomer;

import java.sql.Timestamp;
import java.util.List;

public interface JourneyDiarySignatureService {

    JourneyDiarySignature save(JourneyDiarySignature journeyDiarySignature);

    List<JourneyDiarySignature> saveAll(List<JourneyDiarySignature> journeyDiarySignature);


    Page<JourneyDiarySignature> findByMemberCustomerIdAndTimeEndBetweenOrderByCreatedAtDesc(
            long memberCustomerId, Timestamp fromDate, Timestamp toDate, int page, int size);


    List<JourneyDiarySignature> findByIdIn(List<Long> ids);

    List<JourneyDiarySignature> findByMemberCustomerAndTimeEndBetween(MemberCustomer memberCustomer, Timestamp startTime, Timestamp endTime);

    JourneyDiarySignature findByJourneyDiary(JourneyDiary journeyDiary);

    JourneyDiarySignature findById(Long id);

    void migrateTimeStartAndTimeEnd();

}

