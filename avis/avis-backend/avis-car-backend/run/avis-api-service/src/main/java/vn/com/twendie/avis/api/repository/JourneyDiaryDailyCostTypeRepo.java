package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.JourneyDiaryDailyCostType;

import java.util.Collection;
import java.util.List;

public interface JourneyDiaryDailyCostTypeRepo extends JpaRepository<JourneyDiaryDailyCostType, Long> {

    List<JourneyDiaryDailyCostType> findByJourneyDiaryDailyIdInAndDeletedFalse(Collection<Long> journeyDiaryDailyIds);

}
