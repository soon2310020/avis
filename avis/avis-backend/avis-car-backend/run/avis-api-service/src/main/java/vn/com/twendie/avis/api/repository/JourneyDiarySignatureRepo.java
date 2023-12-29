package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.twendie.avis.data.model.JourneyDiary;
import vn.com.twendie.avis.data.model.JourneyDiarySignature;

import java.util.List;

@Repository
public interface JourneyDiarySignatureRepo extends JpaRepository<JourneyDiarySignature, Long> {

    @Query(value = "select js.* from journey_diary_signature js inner join journey_diary jd on jd.id = js.journey_diary_id " +
            "inner join member_customer mc on js.member_customer_id = mc.id where mc.role != 'Ignore' and jd.id = :journeyDiaryId", nativeQuery = true)
    List<JourneyDiarySignature> findByJourneyDiary(@Param("journeyDiaryId") long journeyDiaryId);

//    JourneyDiarySignature findByJourneyDiary(Long id);
}
