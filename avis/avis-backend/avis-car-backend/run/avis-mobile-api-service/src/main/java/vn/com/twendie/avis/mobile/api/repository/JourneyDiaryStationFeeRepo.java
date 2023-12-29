package vn.com.twendie.avis.mobile.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.JourneyDiaryStationFee;

import java.util.Collection;
import java.util.List;

public interface JourneyDiaryStationFeeRepo extends JpaRepository<JourneyDiaryStationFee, Long> {
    List<JourneyDiaryStationFee> findByJourneyDiaryIdInAndDeletedFalse(Collection<Long> journeyDiaryIds);
}
