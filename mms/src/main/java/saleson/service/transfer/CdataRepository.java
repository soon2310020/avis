package saleson.service.transfer;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import saleson.model.Cdata;

@Repository
public interface CdataRepository extends JpaRepository<Cdata, Long>, QuerydslPredicateExecutor<Cdata>, CdataRepositoryCustom {
	List<Cdata> findAllByCreatedAtBetween(Instant startDate, Instant endDate);

	Optional<Cdata> findFirstByMoldIdAndLstLessThanOrderByLstDesc(Long moldId, String lst);

	List<Cdata> findByCiAndRtGreaterThan(String ci, String rt);

	List<Cdata> findAllByMoldIdAndRtBetweenAndTempNotNullOrderByRtAscLstAsc(Long moldId, String fromTime, String toTime);

	Optional<Cdata> findFirstByMoldIdAndDayAndLstGreaterThanEqualOrderByLstAsc(Long moldId, String day, String matchTime);

	Optional<Cdata> findFirstByMoldIdAndDayAndLstLessThanEqualOrderByLstDesc(Long moldId, String day, String unMatchTime);

	@Modifying
	@Query("UPDATE Cdata SET moldId = :moldId, moldCode = :moldCode WHERE ci = :ci AND moldId IS NULL AND day >= :from AND day <= :to")
	int updateAllMoldIdAndMoldCodeByCiAndMoldIdIsNullAndDayBetween(@Param("ci") String ci, @Param("moldId") Long moldId, @Param("moldCode") String moldCode,
			@Param("from") String from, @Param("to") String to);

	List<Cdata> findByCiAndMoldIdIsNullAndMoldCodeIsNullOrderByIdDesc(String ci);

	List<Cdata> findAllByMoldIdAndTempNotNullOrderByRtAscLstAsc(Long moldId);

}
