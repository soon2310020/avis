package vn.com.twendie.avis.mobile.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.twendie.avis.data.model.JourneyDiary;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface JourneyDiaryRepo extends JpaRepository<JourneyDiary, Long> {

    // TODO: 2020-03-11 refactor this after demo
    @Query(value = "SELECT * FROM journey_diary WHERE contract_id = :contractId AND time_start >= :fromTime AND is_deleted = false ORDER BY time_start DESC",
            countQuery = "SELECT count(*) from journey_diary WHERE contract_id = :contractId AND time_start >= :fromTime AND is_deleted = false",
            nativeQuery = true)
    Page<JourneyDiary> getListJourneyDiaryByContractIdFromTime(@Param("contractId") Long contractId, @Param("fromTime") String fromTime, Pageable pageable);

    @Query(value = "SELECT * FROM journey_diary WHERE contract_id = :contractId AND time_start <= :toTime AND is_deleted = false ORDER BY time_start DESC",
            countQuery = "SELECT count(*) from journey_diary WHERE contract_id = :contractId AND time_start <= :toTime AND is_deleted = false",
            nativeQuery = true)
    Page<JourneyDiary> getListJourneyDiaryByContractIdToTime(@Param("contractId") Long contractId, @Param("toTime") String toTime, Pageable pageable);

    @Query(value = "SELECT * FROM journey_diary WHERE contract_id = :contractId AND time_start >= :fromTime AND time_start <= :toTime AND is_deleted = false ORDER BY time_start DESC",
            countQuery = "SELECT count(*) from journey_diary WHERE contract_id = :contractId AND time_start >= :fromTime AND time_start <= :toTime AND is_deleted = false",
            nativeQuery = true)
    Page<JourneyDiary> getListJourneyDiaryByContractIdWhichInTime(@Param("contractId") Long contractId, @Param("fromTime") String fromTime,
                                                                  @Param("toTime") String toTime, Pageable pageable);

    @Query(value = "SELECT * FROM journey_diary WHERE contract_id = :contractId AND is_deleted = false ORDER BY time_start DESC",
            countQuery = "SELECT count(*) from journey_diary  WHERE contract_id = :contractId AND is_deleted = false",
            nativeQuery = true)
    Page<JourneyDiary> getListJourneyDiaryByContractId(@Param("contractId") Long contractId, Pageable pageable);

    Optional<JourneyDiary> findByIdAndDeletedFalse(Long id);

    List<JourneyDiary> findByTimeEndBetweenAndDeletedFalse(Timestamp timeEndFrom, Timestamp timeEndTo);
}
