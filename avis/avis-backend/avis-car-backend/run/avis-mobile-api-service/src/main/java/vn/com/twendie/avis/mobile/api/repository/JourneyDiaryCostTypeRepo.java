package vn.com.twendie.avis.mobile.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.twendie.avis.data.model.JourneyDiaryCostType;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface JourneyDiaryCostTypeRepo extends JpaRepository<JourneyDiaryCostType, Long> {

    @Query(value = "SELECT SUM(value) " +
            "FROM journey_diary_cost_type " +
            "WHERE journey_diary_id = :journeyDiaryId " +
            "   AND is_deleted = FALSE",
            nativeQuery = true)
    BigDecimal getTotalCostByJourneyDiaryId(@Param("journeyDiaryId") Long journeyDiaryId);

    List<JourneyDiaryCostType> findByJourneyDiaryIdInAndDeletedFalse(Collection<Long> journeyDiaryIds);

}
