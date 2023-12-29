package saleson.api.mold;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import saleson.model.MoldPart;
import saleson.model.Part;
import saleson.model.data.MoldPartYearWeekOrMonth;

import java.util.List;

public interface MoldPartRepository extends JpaRepository<MoldPart, Long>, QuerydslPredicateExecutor<MoldPart> {
	@EntityGraph(attributePaths = {"mold", "part"})
	List<MoldPart> findAllByMoldIdOrderById(Long moldId);

	@Query("SELECT new saleson.model.data.MoldPartYearWeekOrMonth(SUM(s.shotCount*sp.cavity), FUNCTION('YEAR', s.createdAt), FUNCTION('WEEK', s.createdAt)) " +
			" FROM StatisticsPart sp" +
			" INNER JOIN Statistics s ON sp.statisticsId = s.id" +
			" WHERE sp.id IN :ids GROUP BY FUNCTION('YEAR', s.createdAt), FUNCTION('WEEK', s.createdAt)")
	List<MoldPartYearWeekOrMonth> findMoldPartYearWeekly(@Param("ids") List<Long> ids);

	@Query("SELECT new saleson.model.data.MoldPartYearWeekOrMonth(SUM(s.shotCount*sp.cavity), FUNCTION('YEAR', s.createdAt), FUNCTION('MONTH', s.createdAt)) " +
			" FROM StatisticsPart sp" +
			" INNER JOIN Statistics s ON sp.statisticsId = s.id" +
			" WHERE sp.id IN :ids GROUP BY FUNCTION('YEAR', s.createdAt), FUNCTION('MONTH', s.createdAt)")
	List<MoldPartYearWeekOrMonth> findMoldPartYearMonthly(@Param("ids") List<Long> ids);

	@Query("SELECT new saleson.model.data.MoldPartYearWeekOrMonth(SUM(s.shotCount*sp.cavity), FUNCTION('YEAR', s.createdAt)) " +
			" FROM StatisticsPart sp" +
			" INNER JOIN Statistics s ON sp.statisticsId = s.id" +
			" WHERE sp.id IN :ids GROUP BY FUNCTION('YEAR', s.createdAt)")
	List<MoldPartYearWeekOrMonth> findMoldPartYearly(@Param("ids") List<Long> ids);

	void deleteByMoldId(Long moldId);

	List<MoldPart> findByIdIn(List<Long> ids);

	List<MoldPart> findByPart(Part part);

//	@Query(nativeQuery = true, value = "delete from MOLD_PART as m where m.MOLD_ID = ?1")
	void deleteAllByMoldId(Long moldId);

	MoldPart findByMoldIdAndPartId(Long moldId, Long partId);

	List<MoldPart> findAllByMoldIdIn(List<Long> moldIdList);
}
