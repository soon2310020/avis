package vn.com.twendie.avis.mobile.api.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.com.twendie.avis.data.model.JourneyDiaryDaily;

import java.util.List;
import java.util.Set;

@Repository
public interface JourneyDiaryDailyRepo extends JpaRepository<JourneyDiaryDaily, Long> {

    List<JourneyDiaryDaily> findByJourneyDiaryIdOrderByCreatedAtAsc(Long journeyDiaryDailyId);

    @Query(nativeQuery = true, value = "select j.trip_itinerary from journey_diary_daily as j where j.journey_diary_id = :journey_diary_id")
    Set<String> findTripItineraryByJourneyDiaryId(@Param("journey_diary_id") Long journey_diary_id);

}
