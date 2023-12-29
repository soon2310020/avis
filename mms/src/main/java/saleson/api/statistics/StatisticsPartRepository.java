package saleson.api.statistics;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import saleson.model.StatisticsPart;
import saleson.model.data.MiniComponentData;

import java.util.List;
import java.util.Optional;

public interface StatisticsPartRepository extends JpaRepository<StatisticsPart, Long>, QuerydslPredicateExecutor<StatisticsPart> {

	Optional<StatisticsPart> findFirstByPartIdOrderByCreatedAtAsc(Long partId);

	Optional<StatisticsPart> findFirstByPartIdOrderByCreatedAtDesc(Long partId);

	List<StatisticsPart> findByStatisticsIdIn(List<Long> statisticsIds);

	// PartCode가 변경된 경우 통계 데이터도 일괄 업데이트
	@Modifying
	@Transactional
	@Query("UPDATE StatisticsPart SET partCode = :partCode WHERE partCode = :previousPartCode")
	void updatePartCodeAll(@Param("previousPartCode") String previousPartCode, @Param("partCode") String partCode);

	@Query("FROM StatisticsPart WHERE statisticsId IN :statisticsIds")
	List<StatisticsPart> findAllByStatisticsIdIn(@Param("statisticsIds") List<Long> statisticsIds);

	List<StatisticsPart> findByStatisticsId(Long statisticsId);

	@Query("SELECT DISTINCT new saleson.model.data.MiniComponentData(projectId, projectName) FROM StatisticsPart")
	List<MiniComponentData> findAllWithProjectIdProjectName();

	@Query("SELECT COUNT(DISTINCT sp.partId) FROM StatisticsPart sp WHERE sp.statisticsId in :statisticsIds")
	Integer countDistinctPartIdByStatisticsIdIn(List<Long> statisticsIds);

	StatisticsPart findFirstByStatisticsId(Long statisticsId);

	@Query("SELECT sp FROM StatisticsPart sp inner join Statistics s on sp.statisticsId=s.id WHERE sp.partId = :partId ORDER BY sp.createdAt ASC")
	Optional<List<StatisticsPart>> findFirstByPartIdAndStatisticsExistsOrderByCreatedAtAsc(Long partId, Pageable pageable);

	@Query("SELECT sp FROM StatisticsPart sp inner join Statistics s on sp.statisticsId=s.id  WHERE sp.partId = :partId ORDER BY sp.createdAt DESC")
	Optional<List<StatisticsPart>> findFirstByPartIdAndStatisticsExistsOrderByCreatedAtDesc(Long partId, Pageable pageable);

	Optional<StatisticsPart> findFirstByProjectIdOrderByCreatedAtAsc(Long projectId);

}
