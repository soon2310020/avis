package saleson.api.part;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import com.querydsl.core.types.Predicate;

import saleson.common.enumeration.OperatingStatus;
import saleson.dto.SystemNoteParam;
import saleson.model.Part;
import saleson.model.data.MiniComponentData;
import saleson.model.data.PartWithStatisticsData;

public interface PartRepository extends JpaRepository<Part, Long>, QuerydslPredicateExecutor<Part>, PartRepositoryCustom {
	boolean existsPartByPartCode(String partCode);

	boolean existsByCategoryId(Long categoryId);

	List<Part> findAllByCategoryId(Long id);

	Long countAllByCategoryId(Long id);

	List<Part> findByPartCodeIn(List<String> partCodes);

	@Query("SELECT new saleson.model.data.MiniComponentData(id, partCode, name) FROM Part WHERE enabled = true and (deleted is null or deleted = false)")
	List<MiniComponentData> findAllPartIdPartName();
	@Query("SELECT new saleson.model.data.MiniComponentData(id, partCode, name) FROM Part WHERE enabled = true and (deleted is null or deleted = false) and id in :ids")
	List<MiniComponentData> findAllPartIdPartNameByIdIn(@Param("ids") List<Long> ids);

	@Override
	@EntityGraph(attributePaths = "moldParts")
	Page<Part> findAll(Predicate predicate, Pageable pageable);

	@Query("SELECT new saleson.model.data.PartWithStatisticsData(part, SUM(CASE WHEN moldPart.mold.operatingStatus = :op THEN 1 ELSE 0 END))" +
			" FROM Part part" +
			" LEFT JOIN MoldPart moldPart ON moldPart.partId = part.id" +
			" GROUP BY part.id" +
			" ORDER BY SUM(CASE WHEN moldPart.mold.operatingStatus = :op THEN 1 ELSE 0 END) DESC")
	List<PartWithStatisticsData> findPartSortByMoldOPDesc(@Param("op") OperatingStatus operatingStatus, Pageable pageable);

	@Query("SELECT new saleson.model.data.PartWithStatisticsData(part, SUM(CASE WHEN moldPart.mold.operatingStatus = :op THEN 1 ELSE 0 END))" +
			" FROM Part part" +
			" LEFT JOIN MoldPart moldPart ON moldPart.partId = part.id" +
			" GROUP BY part.id" +
			" ORDER BY SUM(CASE WHEN moldPart.mold.operatingStatus = :op THEN 1 ELSE 0 END) ASC")
	List<PartWithStatisticsData> findPartSortByMoldOPAsc(@Param("op") OperatingStatus operatingStatus, Pageable pageable);

	@Query("select new saleson.dto.SystemNoteParam(o.partCode,o.id) from Part o where upper(o.partCode) like :code")
	List<SystemNoteParam> findAllByPartCodeUpperCase(String code);


//	Boolean existsPartByPartCode(String locationCode);
	Boolean existsPartByName(String name);
	Boolean existsPartByPartCodeAndIdNot(String code,Long id);
	Boolean existsPartByNameAndIdNot(String name,Long id);


	List<Part> findByCreatedAtAfterAndCreatedByIn(Instant date, List<Long> userIds);

	Part findFirstByPartCode(String partCode);

	Optional<Part> findByPartCode(String name);
	Optional<Part> findByName(String name);

}
