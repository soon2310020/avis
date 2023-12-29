package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.JourneyDiaryCostType;

import java.util.List;

public interface JourneyDiaryCostTypeRepo extends JpaRepository<JourneyDiaryCostType, Long> {

    List<JourneyDiaryCostType> findByJourneyDiaryIdAndDeletedFalse(Long journeyDiaryId);

}
