package vn.com.twendie.avis.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.twendie.avis.api.model.projection.JDDVehicleInfoProjection;
import vn.com.twendie.avis.api.model.projection.OvertimeInfo;
import vn.com.twendie.avis.data.model.JourneyDiaryDaily;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

public interface JourneyDiaryDailyRepo extends JpaRepository<JourneyDiaryDaily, Long> {

    List<JourneyDiaryDaily> findAllByDeletedFalse();

    @Query(nativeQuery = true, value = "SELECT jd.* from journey_diary_daily jd LEFT JOIN journey_diary j on jd.journey_diary_id = j.id " +
            "LEFT JOIN journey_diary_signature js on js.journey_diary_id = j.id left join member_customer m on m.id = " +
            "js.member_customer_id where jd.date >= :fromDate and jd.date <= :toDate and j.contract_id = :contractId " +
            "and (js.id is null or m.id is null or lower(m.name) like lower (concat('%',:fullName,'%'))) order by jd.date desc ")
    List<JourneyDiaryDaily> search(@Param("fullName") String fullName,
                                   @Param("fromDate") Timestamp fromDate,
                                   @Param("toDate") Timestamp toDate,
                                   @Param("contractId") Long contractId);


    @Query(nativeQuery = true, value = "SELECT jd.* from journey_diary_daily jd LEFT JOIN journey_diary j on jd.journey_diary_id = j.id " +
            "LEFT JOIN journey_diary_signature js on js.journey_diary_id = j.id left join member_customer m on m.id = " +
            "js.member_customer_id where jd.date >= :fromDate and jd.date <= :toDate and j.contract_id = :contractId " +
            "and m.id in :memberCustomerId order by jd.date desc ")
    List<JourneyDiaryDaily> searchByMemberCustomerId(@Param("memberCustomerId") List<Long> memberCustomerIds,
                                   @Param("fromDate") Timestamp fromDate,
                                   @Param("toDate") Timestamp toDate,
                                   @Param("contractId") Long contractId);



    @Query(nativeQuery = true, value = "SELECT jd.* from journey_diary_daily jd LEFT JOIN journey_diary j on jd.journey_diary_id = j.id " +
            "LEFT JOIN journey_diary_signature js on js.journey_diary_id = j.id left join member_customer m on m.id = " +
            "js.member_customer_id where jd.month = :monthSearch and j.contract_id = :contractId " +
            "and lower(m.name) like lower (concat('%',:fullName,'%')) order by jd.date desc ")
    List<JourneyDiaryDaily> searchMonth(@Param("fullName") String fullName,
                                   @Param("monthSearch") Timestamp monthSearch,
                                   @Param("contractId") Long contractId);

    @Query(nativeQuery = true, value = "SELECT jd.* from journey_diary_daily jd LEFT JOIN journey_diary j on jd.journey_diary_id = j.id " +
            "LEFT JOIN journey_diary_signature js on js.journey_diary_id = j.id left join member_customer m on m.id = " +
            "js.member_customer_id where jd.month = :monthSearch and j.contract_id = :contractId " +
            "and m.id in :memberCustomerIds order by jd.date desc ")
    List<JourneyDiaryDaily> searchMonth(@Param("memberCustomerIds") List<Long> memberCustomerIds,
                                        @Param("monthSearch") Timestamp monthSearch,
                                        @Param("contractId") Long contractId);

    List<JourneyDiaryDaily> findAllByContractIdAndDateBetweenAndParentIsNullAndDeletedFalseOrderByDateDescCreatedAtDesc(
            Long contractId, Timestamp fromDate, Timestamp toDate);

    List<JourneyDiaryDaily> findByContractIdAndMonthAndParentIsNullAndDeletedFalseOrderByDateDescCreatedAtDesc(Long contractId, Timestamp month);

    List<JourneyDiaryDaily> findAllByJourneyDiaryIdAndDeletedFalseOrderByDate(Long journeyDiaryId);

    Integer countByParentIdAndDeletedFalse(Long id);

    JourneyDiaryDaily findFirstByParentIdAndDeletedFalse(Long id);

    List<JourneyDiaryDaily> findByParentIdInAndDeletedFalse(Collection<Long> parentIds);

    List<JourneyDiaryDaily> findByContractIdAndDateAndDeletedFalse(Long contractId, Timestamp date);

    List<JourneyDiaryDaily> findAllByContractIdInAndDateBetweenAndParentIsNullAndDeletedFalse(
            Collection<Long> contractIds, Timestamp from, Timestamp to);

    @Query(value = "SELECT jdd " +
            "FROM JourneyDiaryDaily jdd " +
            "WHERE jdd.contractId IN :contractIds " +
            "   AND ( " +
            "       jdd.month IS NULL AND jdd.date BETWEEN :from AND :to " +
            "       OR jdd.month IS NOT NULL AND jdd.month BETWEEN :from AND :to " +
            "   ) " +
            "   AND jdd.parentId IS NULL " +
            "   AND jdd.deleted = FALSE ")
    List<JourneyDiaryDaily> findByContractIdInAndDateOrMonthBetweenAndParentIsNullAndDeletedFalse(
            Collection<Long> contractIds, Timestamp from, Timestamp to);

    @Query(value = "SELECT jdd.id AS id, jdd.vehicle_number_plate AS customNumberPlate, v.number_plate AS numberPlate," +
            " jddct.value AS costValue, ct.code AS costCode, IF(a.name IS NOT NULL, a.name, a2.name) AS accountantName FROM journey_diary_daily jdd" +
            " LEFT JOIN" +
            " journey_diary_daily_cost_type jddct LEFT JOIN cost_type ct ON jddct.cost_type_id = ct.id" +
            " ON jdd.id = jddct.journey_diary_daily_id" +
            " LEFT JOIN" +
            " vehicle v LEFT JOIN user a ON v.accountant_id = a.id AND v.accountant_id IS NOT NULL" +
            " LEFT JOIN branch b ON v.branch_id = b.id" +
            " ON jdd.vehicle_id = v.id OR jdd.vehicle_number_plate = v.number_plate" +
            " LEFT JOIN" +
            " vehicle v2 LEFT JOIN user a2 ON v2.accountant_id = a2.id AND v2.accountant_id IS NOT NULL" +
            " LEFT JOIN branch b2 ON v2.branch_id = b2.id" +
            " ON jdd.vehicle_number_plate = v2.number_plate AND jdd.vehicle_number_plate IS NOT NULL" +
            " LEFT JOIN" +
            " contract c LEFT JOIN contract_type ct2 ON c.contract_type_id = ct2.id" +
            " ON jdd.contract_id = c.id" +
            " LEFT JOIN journey_diary_daily jdd2 ON jdd.id = jdd2.parent_id" +
            " WHERE jdd.date BETWEEN :from AND :to AND jdd.is_deleted = false" +
            " AND (:branchCode IS NULL OR b.code = :branchCode OR b2.code = :branchCode)" +
            " AND ct.code IN :costCodes AND (jdd2.parent_id is null OR jdd2.is_deleted = true)" +
            " AND ct2.id = :contractTypeId", nativeQuery = true)
    List<JDDVehicleInfoProjection> findJDDTicketFeeInfo(Timestamp from, Timestamp to, String branchCode, List<String> costCodes, Long contractTypeId);

    @Query(value = "SELECT jdd.id AS id, jdd.vehicle_number_plate AS customNumberPlate, v.number_plate AS numberPlate," +
            " jddct.value AS costValue, ct.code AS costCode, IF(a.name IS NOT NULL, a.name, a2.name) AS accountantName FROM journey_diary_daily jdd" +
            " LEFT JOIN" +
            " journey_diary_daily_cost_type jddct LEFT JOIN cost_type ct ON jddct.cost_type_id = ct.id" +
            " ON jdd.id = jddct.journey_diary_daily_id" +
            " LEFT JOIN" +
            " vehicle v LEFT JOIN user a ON v.accountant_id = a.id AND v.accountant_id IS NOT NULL" +
            " LEFT JOIN branch b ON v.branch_id = b.id" +
            " ON jdd.vehicle_id = v.id OR jdd.vehicle_number_plate = v.number_plate" +
            " LEFT JOIN" +
            " vehicle v2 LEFT JOIN user a2 ON v2.accountant_id = a2.id AND v2.accountant_id IS NOT NULL" +
            " LEFT JOIN branch b2 ON v2.branch_id = b2.id" +
            " ON jdd.vehicle_number_plate = v2.number_plate AND jdd.vehicle_number_plate IS NOT NULL" +
            " LEFT JOIN" +
            " contract c LEFT JOIN contract_type ct2 ON c.contract_type_id = ct2.id" +
            " ON jdd.contract_id = c.id" +
            " LEFT JOIN journey_diary_daily jdd2 ON jdd.id = jdd2.parent_id" +
            " WHERE jdd.date BETWEEN :from AND :to AND jdd.is_deleted = false" +
            " AND (v.number_plate IN :vehicleNumberPlates OR jdd.vehicle_number_plate IN :vehicleNumberPlates)" +
            " AND (:branchCode IS NULL OR b.code = :branchCode OR b2.code = :branchCode)" +
            " AND ct.code IN :costCodes AND (jdd2.parent_id is null OR jdd2.is_deleted = true)" +
            " AND ct2.id = :contractTypeId", nativeQuery = true)
    List<JDDVehicleInfoProjection> findJDDTicketFeeInfo(Timestamp from, Timestamp to, String branchCode, List<String> costCodes,
                                                        Long contractTypeId, Collection<String> vehicleNumberPlates);

    @Query(value = "SELECT c.id AS contractId," +
            "       SUM(IF(NOT jdd.is_self_drive AND jdd.used_km IS NOT NULL, 1, 0)) AS realWorkingDay," +
            "       SUM(jdd.over_time) AS overtime," +
            "       SUM(jdd.overnight) AS overnight," +
            "       SUM(IF(NOT jdd.is_self_drive AND jdd.used_km IS NOT NULL AND NOT jdd.is_holiday AND jdd.is_weekend, 1, 0)) AS weekend," +
            "       SUM(IF(!jdd.is_self_drive AND jdd.used_km IS NOT NULL AND jdd.is_holiday, 1, 0)) AS holiday " +
            "FROM journey_diary_daily jdd" +
            "         LEFT JOIN contract c ON jdd.contract_id = c.id " +
            "WHERE c.id IN :contractIds" +
            "  AND c.to_datetime >= :from" +
            "  AND c.from_datetime <= :to" +
            "  AND NOT c.is_deleted" +
            "  AND jdd.date BETWEEN :from AND :to" +
            "  AND jdd.parent_id IS NULL" +
            "  AND NOT jdd.is_deleted " +
            "GROUP BY c.id",
            nativeQuery = true)
    List<OvertimeInfo> findOvertimeInfos(Collection<Long> contractIds, Timestamp from, Timestamp to);

    JourneyDiaryDaily findFirstByJourneyDiaryIdAndDeletedFalseOrderById(Long id);

    JourneyDiaryDaily findFirstByJourneyDiaryIdAndDeletedFalseOrderByIdDesc(Long id);

    List<JourneyDiaryDaily> findByContractIdIn(List<Long> ids);

    @Query(value = "SELECT * " +
            "FROM (" +
            "         SELECT *, row_number() OVER (ORDER BY date) AS row_num" +
            "         FROM journey_diary_daily" +
            "         WHERE contract_id = :contractId" +
            "           AND parent_id IS NULL" +
            "           AND km_start IS NOT NULL" +
            "           AND is_self_drive = FALSE" +
            "           AND is_deleted = FALSE" +
            "           AND date BETWEEN :from AND :to" +
            "     ) jdd " +
            "WHERE row_num <= :workingDayValue AND jdd.is_over_day" +
            "   OR row_num > :workingDayValue AND NOT jdd.is_over_day",
            nativeQuery = true)
    List<JourneyDiaryDaily> findWrongOverDayJourneyDairyDaily(Long contractId, Integer workingDayValue, Timestamp from, Timestamp to);

    Page<JourneyDiaryDaily> findByContractContractTypeIdAndContractWorkingDayId(Long id, Long workingDayId, Pageable pageable);

    Page<JourneyDiaryDaily> findByParentIsNotNullAndDateIsNotNull(Pageable pageable);

    Page<JourneyDiaryDaily> findByParentIsNullAndDateIsNotNull(Pageable pageable);
    Page<JourneyDiaryDaily> findByDateBetween(Timestamp from, Timestamp to,Pageable pageable);

}
