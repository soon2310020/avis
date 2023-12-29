package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.com.twendie.avis.api.model.projection.FirstJourneyDiaryDateProjection;
import vn.com.twendie.avis.data.model.JourneyDiary;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

public interface JourneyDiaryRepo extends JpaRepository<JourneyDiary, Long> {

    boolean existsByVehicleIdAndStepNotAndDeletedFalse(Long driverId, int step);

    boolean existsByContractIdAndStepNotAndDeletedFalse(Long contractId, int step);

    List<JourneyDiary> findByContractIdAndTimeStartAfterAndDeletedFalseOrderById(Long contractId, Timestamp timestamp);

    List<JourneyDiary> findByContractIdAndDeletedFalseOrderById(Long contractId);

    List<JourneyDiary> findByContractIdAndIdGreaterThanAndDeletedFalseOrderById(Long contractId, Long id);

    @Query("SELECT jdd.contractId AS contractId," +
            "   MIN(jdd.timeStart) AS firstJourneyDiaryDate " +
            "FROM JourneyDiary jdd " +
            "WHERE jdd.contractId IN :contractIds " +
            "   AND jdd.deleted = FALSE " +
            "GROUP BY jdd.contractId ")
    List<FirstJourneyDiaryDateProjection> findFirstJourneyDiaryDate(Collection<Long> contractIds);

}
