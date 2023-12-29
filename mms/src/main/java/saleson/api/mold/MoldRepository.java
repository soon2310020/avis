package saleson.api.mold;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.persistence.LockModeType;

import com.querydsl.core.types.Predicate;
import org.h2.store.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.OperatingStatus;
import saleson.dto.MapToolingDTO;
import saleson.dto.SystemNoteParam;
import saleson.model.Mold;
import saleson.model.data.CategorySummary;

public interface MoldRepository extends JpaRepository<Mold, Long>, QuerydslPredicateExecutor<Mold>, MoldRepositoryCustom {

	List<Mold> findAllByOrderByIdDesc();

	List<Mold> findByIdInOrderByIdDesc(List<Long> ids);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Mold> findWithPessimisticLockById(Long id);

	boolean existsByEquipmentCode(String equipmentCode);

	boolean existsByEquipmentCodeAndIdNot(String equipmentCode, Long id);

	Mold findByEquipmentCode(String equipmentCode);

	Mold findByCounterId(Long counterId);

	Optional<Mold> findFirstByEquipmentCode(String equipmentCode);

	List<Mold> findAllByOperatingStatus(OperatingStatus working);

	List<Mold> findAllByEquipmentStatusIn(List<EquipmentStatus> equipmentStatusList);
	/*
		M01
		boolean existsByPartId(Long id);
	*/

	@Query(value = "" + //
			"	SELECT                									\n" + //
			"	       C.NAME AS CATEGORY,                				\n" + //
			"	       COUNT(PROJECT_ID) AS PROJECT_COUNT,          	\n" + //
			"	       SUM(PART_COUNT) AS PART_COUNT,                	\n" + //
			"	       SUM(MOLD_COUNT) AS MOLD_COUNT                	\n" + //
			"	FROM (                									\n" + //
			"	    SELECT                								\n" + //
			"	        C.ID AS PROJECT_ID,                				\n" + //
			"	        C.PARENT_ID,                					\n" + //
			"	        COUNT(PART_ID) AS PART_COUNT,                	\n" + //
			"	        SUM(MOLD_COUNT) AS MOLD_COUNT                	\n" + //
			"	    FROM (                								\n" + //
			"	        SELECT                							\n" + //
			"	            P.ID AS PART_ID,                			\n" + //
			"	            P.CATEGORY_ID,                				\n" + //
			"	            COUNT(M.ID) MOLD_COUNT                		\n" + //
			"	        FROM MOLD M                						\n" + //
			"	            INNER JOIN PART P ON M.PART_ID = P.ID  		\n" + //
			"	        GROUP BY P.ID, P.CATEGORY_ID                	\n" + //
			"	    ) P                									\n" + //
			"	    INNER JOIN CATEGORY C ON P.CATEGORY_ID = C.ID     	\n" + //
			"	    GROUP BY C.ID, C.PARENT_ID                			\n" + //
			"	) PJ                									\n" + //
			"	INNER JOIN CATEGORY C ON PJ.PARENT_ID = C.ID           	\n" + //
			"	GROUP BY C.ID											", nativeQuery = true)
	List<CategorySummary> findCategorySummaryAll();

	@Query(value = "" + //
			"	SELECT                									\n" + //
			"	       C.NAME AS CATEGORY,                				\n" + //
			"	       COUNT(PROJECT_ID) AS PROJECT_COUNT,          	\n" + //
			"	       SUM(PART_COUNT) AS PART_COUNT,                	\n" + //
			"	       SUM(MOLD_COUNT) AS MOLD_COUNT                	\n" + //
			"	FROM (                									\n" + //
			"	    SELECT                								\n" + //
			"	        C.ID AS PROJECT_ID,                				\n" + //
			"	        C.PARENT_ID,                					\n" + //
			"	        COUNT(PART_ID) AS PART_COUNT,                	\n" + //
			"	        SUM(MOLD_COUNT) AS MOLD_COUNT                	\n" + //
			"	    FROM (                								\n" + //
			"	        SELECT                							\n" + //
			"	            P.ID AS PART_ID,                			\n" + //
			"	            P.CATEGORY_ID,                				\n" + //
			"	            COUNT(M.ID) MOLD_COUNT                		\n" + //
			"	        FROM MOLD M                						\n" + //
			"	            INNER JOIN PART P ON M.PART_ID = P.ID  		\n" + //
			"	        WHERE M.COMPANY_ID = = :companyId               \n" + //
			"	        GROUP BY P.ID, P.CATEGORY_ID                	\n" + //
			"	    ) P                									\n" + //
			"	    INNER JOIN CATEGORY C ON P.CATEGORY_ID = C.ID     	\n" + //
			"	    GROUP BY C.ID, C.PARENT_ID                			\n" + //
			"	) PJ                									\n" + //
			"	INNER JOIN CATEGORY C ON PJ.PARENT_ID = C.ID           	\n" + //
			"	GROUP BY C.ID											", nativeQuery = true)
	List<CategorySummary> findCategorySummaryAllByCompanyId(@Param("companyId") Long companyId);

	long countByEquipmentStatus(EquipmentStatus installed);

	long countByEquipmentStatusAndCompanyId(EquipmentStatus installed, Long companyId);

	@Query(nativeQuery = true, value = "\n" + //
			"SELECT \n" + //
			"    mold0_.ID AS moldId,\n" + //
			"    mold0_.EQUIPMENT_CODE AS moldCode,\n" + //
			"    COALESCE(SUM(statistics2_.SHOT_COUNT * statistics3_.CAVITY),\n" + //
			"            0) AS quantityProduced,\n" + //
			"    COALESCE(mold0_.CREATED_AT, ADDDATE(NOW(), - 7)) AS moldCreatedAt,\n" + //
			"    SUM(statistics2_.UPTIME_SECONDS) AS value,\n" + //

			" IF((:startDate != NULL AND :endDate != NULL),\n" + //
			"        IF(mold0_.CREATED_AT < TIMESTAMP(:startDate),\n" + //
			"            :duaTime,\n" + //
			"            TIMESTAMPDIFF(SECOND,\n" + //
			"                TIMESTAMP(:endDate),\n" + //
			"                mold0_.CREATED_AT)),\n" + //
			"        TIMESTAMPDIFF(SECOND,\n" + //
			"            COALESCE(mold0_.CREATED_AT, ADDDATE(NOW(), - 7)),\n" + //
			"            NOW())) AS aliveTime," +

			"    SUM(statistics2_.UPTIME_SECONDS) * 100 / IF((:startDate != NULL AND :endDate != NULL),\n" + //
			"        IF(mold0_.CREATED_AT < TIMESTAMP(:startDate),\n" + //
			"            :duaTime,\n" + //
			"            TIMESTAMPDIFF(SECOND,\n" + //
			"                TIMESTAMP(:endDate),\n" + //
			"                mold0_.CREATED_AT)),\n" + //
			"        TIMESTAMPDIFF(SECOND,\n" + //
			"            COALESCE(mold0_.CREATED_AT, ADDDATE(NOW(), - 7)),\n" + //
			"            NOW())) AS percentage\n" + //
			"FROM\n" + //
			"    MOLD mold0_\n" + //
			"        INNER JOIN\n" + //
			"    MOLD_PART moldpart1_ ON (moldpart1_.MOLD_ID = mold0_.ID)\n" + //
			"        LEFT OUTER JOIN\n" + //
			"    STATISTICS statistics2_ ON (statistics2_.MOLD_ID = mold0_.ID)\n" + //
			"        LEFT OUTER JOIN\n" + //
			"    STATISTICS_PART statistics3_ ON (statistics2_.ID = statistics3_.STATISTICS_ID)\n" + //
			"WHERE\n" + //
			"    1 = 1\n" + //
			"        AND ( COALESCE(:ops) IS NULL OR mold0_.OPERATING_STATUS IN :ops)\n" + //
			"        AND (:startDate IS NULL\n" + //
			"        OR statistics2_.CREATED_AT BETWEEN TIMESTAMP(:startDate) AND TIMESTAMP(:endDate))\n" + //
			"GROUP BY mold0_.ID\n" + //
			"HAVING (:rateFrom is null OR percentage >= :rateFrom) and (:rateTo is null OR percentage < :rateTo)"
			+ " and (:rarelyFrom is null OR percentage > :rarelyFrom) and (:isNever != true OR (percentage = 0 or percentage is null))  \n" + //
			"  order by value DESC " + " LIMIT :pageFrom, :pageSize ")
	List<Object[]> findReportDataWithRateCapacity(@Param("startDate") String startDate, //
			@Param("endDate") String endDate, //
			@Param("duaTime") Long duaTime, //
			@Param("ops") List<String> ops, //
			@Param("rateFrom") Double rateFrom, //
			@Param("rateTo") Double rateTo, //
			@Param("rarelyFrom") Double rarelyFrom, //
			@Param("isNever") Boolean isNever, //
			@Param("pageFrom") Integer pageFrom, //
			@Param("pageSize") Integer pageSize//
//			Pageable pageable//
	);

	@Query(nativeQuery = true, value = "\n" + //
			" select count(*) as total FROM (" + //
			" SELECT \n" + //
			"    mold0_.ID AS moldId,\n" + //
			"    mold0_.EQUIPMENT_CODE AS moldCode,\n" + //
			"    COALESCE(SUM(statistics2_.SHOT_COUNT * statistics3_.CAVITY),\n" + //
			"            0) AS quantityProduced,\n" + //
			"    COALESCE(mold0_.CREATED_AT, ADDDATE(NOW(), - 7)) AS moldCreatedAt,\n" + //
			"    SUM(statistics2_.UPTIME_SECONDS) AS valueUptime,\n" + //

			" IF((:startDate != NULL AND :endDate != NULL),\n" + //
			"        IF(mold0_.CREATED_AT < TIMESTAMP(:startDate),\n" + //
			"            :duaTime,\n" + //
			"            TIMESTAMPDIFF(SECOND,\n" + //
			"                TIMESTAMP(:endDate),\n" + //
			"                mold0_.CREATED_AT)),\n" + //
			"        TIMESTAMPDIFF(SECOND,\n" + //
			"            COALESCE(mold0_.CREATED_AT, ADDDATE(NOW(), - 7)),\n" + //
			"            NOW())) AS aliveTime," +

			"    SUM(statistics2_.UPTIME_SECONDS) * 100 / IF((:startDate != NULL AND :endDate != NULL),\n" + //
			"        IF(mold0_.CREATED_AT < :startDate,\n" + //
			"            :duaTime,\n" + //
			"            TIMESTAMPDIFF(SECOND,\n" + //
			"                :endDate,\n" + //
			"                mold0_.CREATED_AT)),\n" + //
			"        TIMESTAMPDIFF(SECOND,\n" + //
			"            COALESCE(mold0_.CREATED_AT, ADDDATE(NOW(), - 7)),\n" + //
			"            NOW())) AS percentage\n" + //
			"FROM\n" + //
			"    MOLD mold0_\n" + //
			"        INNER JOIN\n" + //
			"    MOLD_PART moldpart1_ ON (moldpart1_.MOLD_ID = mold0_.ID)\n" + //
			"        LEFT OUTER JOIN\n" + //
			"    STATISTICS statistics2_ ON (statistics2_.MOLD_ID = mold0_.ID)\n" + //
			"        LEFT OUTER JOIN\n" + //
			"    STATISTICS_PART statistics3_ ON (statistics2_.ID = statistics3_.STATISTICS_ID)\n" + //
			"WHERE\n" + //
			"    1 = 1\n" + //
			"        AND (COALESCE(:ops) IS NULL OR mold0_.OPERATING_STATUS IN :ops)\n" + //
			"        AND (:startDate IS NULL\n" + //
			"        OR statistics2_.CREATED_AT BETWEEN TIMESTAMP(:startDate) AND TIMESTAMP(:endDate))\n" + //
			"GROUP BY mold0_.ID\n" + //
			"HAVING (:rateFrom is null OR percentage >= :rateFrom) and (:rateTo is null OR percentage < :rateTo)"
			+ " and (:rarelyFrom is null OR percentage > :rarelyFrom) and (:isNever != true OR (percentage = 0 or percentage is null))  \n" + //
			"  ) as report ")
	long countReportDataWithRateCapacity(@Param("startDate") String startDate, //
			@Param("endDate") String endDate, //
			@Param("duaTime") Long duaTime, //
			@Param("ops") List<String> ops, //
			@Param("rateFrom") Double rateFrom, //
			@Param("rateTo") Double rateTo, //
			@Param("rarelyFrom") Double rarelyFrom, //
			@Param("isNever") Boolean isNever//
//			@Param("pageFrom") Integer pageFrom,//
//			@Param("pageSize") Integer pageSize,//
//			Pageable pageable//
	);

	@Query("select new saleson.dto.SystemNoteParam(o.equipmentCode,o.id) from Mold o where upper(equipmentCode) like :code")
	List<SystemNoteParam> findAllByEquipmentCodeUpperCase(String code);

	@Query("select new saleson.dto.MapToolingDTO(o.equipmentCode, o.operatingStatus, o.location.latitude, o.location.longitude, o.location.name) from Mold o where upper(o.equipmentCode) = :equipmentCode and o.operatingStatus is not null and o.equipmentStatus in ('INSTALLED', 'DETACHED')")
	MapToolingDTO getAllToolingMapData(String equipmentCode);

	@Query(value = "select distinct (m.ID) from part p " + //
			"    inner join mold_part mp on p.ID = mp.PART_ID " + //
			"    inner join mold m on mp.MOLD_ID = m.ID " + //
			"where m.OPERATING_STATUS is not null " + //
			"    and m.EQUIPMENT_STATUS in ('INSTALLED', 'DETACHED') " + //
			"    and p.PART_CODE like '%:partCode%'", nativeQuery = true)
	List<Long> Search(@Param("partCode") String partCode);

	List<Mold> findByCreatedAtAfterAndCreatedByIn(Instant date, List<Long> userIds);

	List<Mold> findByLastShotMadeAtBeforeAndDeletedIsFalse(Instant moment);

	Optional<Mold> findByIdAndLastShotMadeAtBeforeAndDeletedIsFalse(Long moldId, Instant moment);

	List<Mold> findAllByEquipmentCodeOrderByIdDesc(String equipmentCode);

	void deleteAllByIdIn(List<Long> idList);

	List<Mold> findAllByCompanyIdAndIdIn(Long companyId, List<Long> ids);

	@Query(nativeQuery = true, value = "SELECT COUNT(*) FROM MOLD mld INNER JOIN COUNTER cnt on cnt.ID = mld.COUNTER_ID AND (cnt.EQUIPMENT_CODE LIKE 'SC%' OR cnt.EQUIPMENT_CODE LIKE 'EM%') WHERE mld.ID = :id")
	long count3rdGenerationDeviceById(@Param("id") Long id);

	Optional<Mold> findByMachineId(Long machineId);

	@Query("select m.id from Mold m where m.deleted is false")
	List<Long> getAllMoldIdByDeletedIsFalse();

	List<Mold> findAllByIdIn(List<Long> ids, Pageable page);
}
