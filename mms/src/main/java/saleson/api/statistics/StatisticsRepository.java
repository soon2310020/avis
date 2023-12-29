package saleson.api.statistics;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import saleson.model.Statistics;

public interface StatisticsRepository extends JpaRepository<Statistics, Long>, QuerydslPredicateExecutor<Statistics>, StatisticsRepositoryCustom {

	@Query("SELECT SUM(uptimeSeconds) FROM Statistics WHERE ci = :ci AND day = :today")
	Long getUptimeSecondsByCiAndDay(@Param("ci") String ci, @Param("today") String today);

	@Deprecated
	List<Statistics> findByDay(String theDay);

	Page<Statistics> findAllByDayAndIdIsGreaterThan(String day, Long lastId, Pageable pageable);

	@Deprecated
	List<Statistics> findByWeek(String theWeek);

	Page<Statistics> findAllByWeekAndIdIsGreaterThan(String week, Long lastId, Pageable pageable);

	@Deprecated
	List<Statistics> findByMonth(String theMonth);

	Page<Statistics> findAllByMonthAndIdIsGreaterThan(String month, Long lastId, Pageable pageable);

	@Deprecated
	List<Statistics> findByYear(String theYear);

	Page<Statistics> findAllByYearAndIdIsGreaterThan(String year, Long lastId, Pageable pageable);

	Optional<Statistics> findFirstByMoldIdOrderByDayAsc(Long moldId);

	Optional<Statistics> findFirstByMoldIdAndCreatedAtIsGreaterThanEqualOrderByDayAsc(Long moldId, Instant lastMaintenanceTime);

	Optional<Statistics> findFirstByMoldIdOrderByDayDesc(Long moldId);

	Optional<Statistics> findFirstByMoldIdAndCtGreaterThanOrderByDayDesc(Long moldId, Double ct);

	List<Statistics> findByHourIsNull();

	boolean existsByMoldIdAndDay(Long moldId, String day);

	boolean existsByMoldIdAndDayAndCiLike(Long moldId, String day, String pattern);

	boolean existsByMoldIdAndWeek(Long moldId, String week);

	boolean existsByMoldIdAndWeekAndCiLike(Long moldId, String week, String pattern);

	boolean existsByMoldIdAndMonth(Long moldId, String month);

	boolean existsByMoldIdAndMonthAndCiLike(Long moldId, String month, String pattern);

	List<Statistics> findByDayBetweenAndMoldId(String fromDay, String toDay, Long moldId);

	Optional<Statistics> findFirstByDayIsNotNullOrderByDayAsc();

	Optional<Statistics> findFirstByCdataId(Long cdataId);

	Optional<Statistics> findFirstByMoldIdOrderByLstAsc(Long moldId);

	Optional<Statistics> findFirstByMoldIdAndScGreaterThanOrderByScAscIdAsc(Long moldId, Integer Sc);

	Optional<Statistics> findFirstByMoldIdAndScGreaterThanOrderByLstAsc(Long moldId, Integer scOverDue);

	List<Statistics> findByCdataIdIn(List<Long> cdataId);

	Optional<Statistics> findFirstByMoldIdAndLstLessThanEqualOrderByScDesc(Long moldId, String startTime);

	Optional<Statistics> findFirstByMoldIdAndDayAndLstGreaterThanEqualOrderByLstAsc(Long moldId, String day, String matchTime);

	Optional<Statistics> findFirstByMoldIdAndDayAndLstLessThanEqualOrderByLstDesc(Long moldId, String day, String unMatchTime);

	Optional<Statistics> findFirstByCiOrderByIdDesc(String ci);

	Optional<List<Statistics>> findByMoldIdAndDay(Long moldId, String day);

	Optional<List<Statistics>> findByMoldIdAndDayAndLstGreaterThanEqual(Long moldId, String day, String firstShotTime);

	Optional<List<Statistics>> findByMoldIdAndDayAndLstLessThanEqual(Long moldId, String day, String lastShotTime);

	Long countAllByCdataIdIsNotNullAndMoldIdIsNotNull();

	Page<Statistics> findAllByCdataIdIsNotNullAndMoldIdIsNotNull(Pageable pageable);

	Page<Statistics> findAllByCdataIdIsNotNullAndMoldIdIsNotNullAndShotCountValIsNull(Pageable pageable);

	Page<Statistics> findAllByMoldIdAndShotCountGreaterThanAndMonthEqualsOrderByLstDesc(Long moldId, Integer minShotCount, String month, Pageable pageable);

	Page<Statistics> findAllByMoldIdAndShotCountGreaterThanAndMonthGreaterThanEqualAndMonthLessThanEqualOrderByLstDesc(Long moldId, Integer minShotCount, String monthFrom,
			String monthTo, Pageable pageable);

	Page<Statistics> findAllByMoldIdAndShotCountGreaterThanAndCtGreaterThanAndMonthGreaterThanEqualAndMonthLessThanEqualOrderByLstDesc(Long moldId, Integer minShotCount,
			double minCt, String monthFrom, String monthTo, Pageable pageable);

	Optional<List<Statistics>> findByMoldIdAndLstGreaterThanEqual(Long moldId, String firstShotTime);

	Optional<List<Statistics>> findByMoldIdAndLstBetween(Long moldId, String start, String end);

	@Query("SELECT SUM(s.ct*s.shotCount) FROM Statistics s WHERE s.moldId = :moldId")
	Double sumCtMultiplyShotCountByMoldId(@Param("moldId") Long moldId);

	@Query("SELECT SUM(s.shotCount) FROM Statistics s WHERE s.moldId = :moldId")
	Integer sumShotCountByMoldId(@Param("moldId") Long moldId);

	@Deprecated
	@Query("SELECT SUM(s.ct*s.shotCount)/ SUM(s.shotCount) FROM Statistics s WHERE s.moldId = :moldId AND s.ct>0 and s.shotCount>0")
	Double calcWeightedAverageCycleTimeByMoldId(@Param("moldId") Long moldId);

	@Deprecated
	@Query("SELECT s FROM Statistics s WHERE s.moldId = :moldId and (s.ctVal > 0 OR s.firstData = true) ORDER BY s.day ASC")
	Optional<List<Statistics>> findFirstByMoldIdAndCtValGreaterThanOrFirstDataIsTrueOrderByDayAsc(@Param("moldId") Long moldId, Pageable pageable);

	@Deprecated
	@Query("SELECT s FROM Statistics s WHERE s.moldId = :moldId and (s.ctVal > 0 OR s.firstData = true) ORDER BY s.day DESC")
	Optional<List<Statistics>> findFirstByMoldIdAndCtValGreaterThanOrFirstDataIsTrueOrderByDayDesc(@Param("moldId") Long moldId, Pageable pageable);

	@Query("SELECT s FROM Statistics s WHERE s.moldId in :moldIdList and (s.ctVal > 0 OR s.firstData = true) ORDER BY s.day ASC")
	Optional<List<Statistics>> findFirstByMoldIdListAndCtValGreaterThanOrFirstDataIsTrueOrderByDayAsc(@Param("moldIdList") List<Long> moldIdList, Pageable pageable);

	@Query("SELECT s FROM Statistics s WHERE s.moldId in :moldIdList and (s.ctVal > 0 OR s.firstData = true) ORDER BY s.day DESC")
	Optional<List<Statistics>> findFirstByMoldIdListAndCtValGreaterThanOrFirstDataIsTrueOrderByDayDesc(@Param("moldIdList") List<Long> moldIdList, Pageable pageable);

	@Query("SELECT s FROM Statistics s WHERE s.moldId = :moldId and (s.ctVal > 0 OR s.firstData = true) AND s.shotCountVal IS NOT NULL AND s.shotCountVal > 0 ORDER BY s.lst DESC")
	Optional<List<Statistics>> findFirstByMoldIdAndCtValGreaterThanOrFirstDataIsTrueOrderByLstDesc(@Param("moldId") Long moldId, Pageable pageable);

	@Deprecated
	@Query("SELECT s FROM Statistics s WHERE s.moldId = :moldId and (s.ct > 0 OR s.firstData = true) ORDER BY s.day ASC")
	Optional<List<Statistics>> findFirstByMoldIdAndCtGreaterThanOrFirstDataIsTrueOrderByDayAsc(@Param("moldId") Long moldId, Pageable pageable);

	@Deprecated
	@Query("SELECT s FROM Statistics s WHERE s.moldId = :moldId and (s.ct > 0 OR s.firstData = true) AND s.shotCount IS NOT NULL AND s.shotCount > 0 ORDER BY s.day DESC")
	Optional<List<Statistics>> findFirstByMoldIdAndCtGreaterThanOrFirstDataIsTrueOrderByDayDesc(@Param("moldId") Long moldId, Pageable pageable);

	@Query("SELECT s FROM Statistics s WHERE s.moldId in :moldIdList and (s.ct > 0 OR s.firstData = true) ORDER BY s.day ASC")
	Optional<List<Statistics>> findFirstByMoldIdListAndCtGreaterThanOrFirstDataIsTrueOrderByDayAsc(@Param("moldIdList") List<Long> moldIdList, Pageable pageable);

	@Query("SELECT s FROM Statistics s WHERE s.moldId in :moldIdList and (s.ct > 0 OR s.firstData = true) AND s.shotCount IS NOT NULL AND s.shotCount > 0 ORDER BY s.day DESC")
	Optional<List<Statistics>> findFirstByMoldIdListAndCtGreaterThanOrFirstDataIsTrueOrderByDayDesc(@Param("moldIdList") List<Long> moldIdList, Pageable pageable);

	@Query("SELECT s FROM Statistics s WHERE s.moldId = :moldId and (s.ct > 0 OR s.firstData = true) ORDER BY s.lst DESC")
	Optional<List<Statistics>> findFirstByMoldIdAndCtGreaterThanOrFirstDataIsTrueOrderByLstDesc(@Param("moldId") Long moldId, Pageable pageable);

//	UPDATE
//		statistics stt,
//		cdata cdt
//	SET
//		stt.MOLD_ID = cdt.MOLD_ID,
//		stt.MOLD_CODE = cdt.MOLD_CODE
//	WHERE
//		cdt.ID = stt.CDATA_ID
//		AND cdt.MOLD_ID IS NOT NULL
//		AND stt.MOLD_ID IS NULL;
	@Modifying
	@Query(nativeQuery = true, value = "UPDATE statistics stt, cdata cdt SET stt.MOLD_ID = cdt.MOLD_ID, stt.MOLD_CODE = cdt.MOLD_CODE "
			+ "WHERE cdt.ID = stt.CDATA_ID AND cdt.MOLD_ID IS NOT NULL AND stt.MOLD_ID IS NULL AND stt.DAY >= :day")
	int updateAllMoldIdAndMoldCodeByCdataAndMoldIdIsNullAndDayGreaterThanEqual(@Param("day") String day);

	@Query("SELECT DISTINCT ci FROM Statistics WHERE moldId IS NULL AND cdataId IS NOT NULL AND day >= :day")
	List<String> findDistinctCiByMoldIdIsNullAndCdataIdIsNotNullAndDayGreaterThanEqual(@Param("day") String day, Pageable pageable);

	Optional<Statistics> findFirstByCiAndMoldIdIsNotNullAndDayGreaterThanEqualOrderByHourAsc(String ci, String day);

	List<Statistics> findByCiAndMoldIdIsNullAndMoldCodeIsNullOrderByIdDesc(String ci);

	Optional<Statistics> findFirstByCiAndIdLessThanOrderByIdDesc(String ci, Long id);

	List<Statistics> findByCi(String ci);

	@Modifying
	@Query("UPDATE Statistics SET moldId = :moldId, moldCode = :moldCode WHERE ci = :ci AND moldId IS NULL AND cdataId IS NOT NULL AND day >= :from AND day <= :to")
	int updateAllMoldIdAndMoldCodeByCiAndMoldIdIsNullAndCdataIdIsNotNullAndDayBetween(@Param("ci") String ci, @Param("moldId") Long moldId, @Param("moldCode") String moldCode,
			@Param("from") String from, @Param("to") String to);

	List<Statistics> findByMoldIdOrderByHourAsc(Long moldId);

	List<Statistics> findByDayAndMoldId(String theDay,Long moldId);


}
