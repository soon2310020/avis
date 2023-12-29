package vn.com.twendie.avis.mobile.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.data.model.JourneyDiarySignature;

import java.sql.Timestamp;
import java.util.List;

public interface JourneyDiarySignatureRepo extends JpaRepository<JourneyDiarySignature, Long> {

    Page<JourneyDiarySignature> findByMemberCustomerIdAndTimeEndBetweenOrderByCreatedAtDesc(
            long memberCustomerId, Timestamp fromDate, Timestamp toDate, Pageable pageable);

    List<JourneyDiarySignature> findByIdIn(List<Long> ids);

    JourneyDiarySignature findFirstByJourneyDiary(JourneyDiary journeyDiary);

}
