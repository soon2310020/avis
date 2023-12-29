package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import vn.com.twendie.avis.api.model.projection.ContractCodeProjection;
import vn.com.twendie.avis.api.model.projection.ContractInfoForNotiProjection;
import vn.com.twendie.avis.data.model.Contract;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ContractRepo extends JpaRepository<Contract, Long>, JpaSpecificationExecutor<Contract> {

    String queryGetAllAvailableContractInfoForNoti = "SELECT id AS contractId, driver_id AS driverId, code AS contractCode FROM contract WHERE is_deleted = false"
            + " AND CURRENT_TIMESTAMP < TIMESTAMP(DATE_ADD(to_datetime,INTERVAL 1 day ))"
            + " AND driver_id IS NOT NULL"
            + " AND vehicle_is_transferred_another = false"
            + " AND driver_is_transferred_another = false";

    @Query(value = "SELECT c.prefix_code AS code, MAX(c.suffix_code) AS subCode" +
            " FROM contract AS c" +
            " WHERE c.prefix_code LIKE :prefixLike" +
            "   AND (:deleted IS NULL OR c.is_deleted = :deleted) " +
            " GROUP BY c.prefix_code" +
            " ORDER BY CHAR_LENGTH(c.prefix_code)",
            nativeQuery = true)
    List<ContractCodeProjection> getContractCodeByPrefixLikeAndDeleted(String prefixLike, Boolean deleted);

    boolean existsByCode(String code);

    int countByContractTypeIdAndStatusAndDeletedFalse(long contractTypeId, int status);

    int countByContractTypeIdAndStatusAndVehicleIsTransferredAnotherAndDriverIsTransferredAnotherAndDeletedFalse(
            long contractTypeId, int status, boolean lendingVehicle, boolean lendingDriver);

    int countByContractTypeIdAndVehicleIsTransferredAnotherAndDriverIsTransferredAnotherAndDeletedFalse(
            long contractTypeId, Boolean lendingVehicle, Boolean lendingDriver);

    @Query(value = "SELECT * FROM contract WHERE is_deleted = false" +
            " AND driver_id = ?1" +
            " AND driver_is_transferred_another = true" +
            " AND status <> ?2" +
            " AND CURRENT_TIMESTAMP < TIMESTAMP(DATE_ADD(to_datetime,INTERVAL 1 day ))" +
            " ORDER BY id DESC limit 1", nativeQuery = true)
    Contract findContractLendingDriver(Long driverId, Integer status);

    @Query(value = "SELECT * FROM contract WHERE is_deleted = false" +
            " AND vehicle_id = ?1" +
            " AND vehicle_is_transferred_another = true" +
            " AND status <> ?2" +
            " AND CURRENT_TIMESTAMP < TIMESTAMP(DATE_ADD(to_datetime,INTERVAL 1 day ))" +
            " ORDER BY id DESC limit 1", nativeQuery = true)
    Contract findContractLendingVehicle(Long vehicleId, Integer status);

    Optional<Contract> findByIdAndDeletedFalse(Long contractId);

    List<Contract> findAllByContractTypeIdInAndToDatetimeAfterAndFromDatetimeBeforeAndDeletedFalse(
            Collection<Long> contractTypeIds, Timestamp from, Timestamp to);

    List<Contract> findAllByFromDatetimeBeforeAndStatusAndDeletedFalse(Timestamp fromDatetimeBefore, Integer status);

    List<Contract> findAllByToDatetimeBeforeAndStatusInAndDeletedFalse(Timestamp toDatetimeBefore, Collection<Integer> status);

    Boolean existsByCustomerIdAndDeletedFalse(Long customerId);

    Boolean existsByMemberCustomerIdAndDeletedFalse(Long memberCustomerId);

    @Query(value = "SELECT id from contract where member_customer_id in ?1 and is_deleted = false", nativeQuery = true)
    List<Long> findContractIdsByMemberCustomerIdIn(List<Long> ids);

    @Query(value = queryGetAllAvailableContractInfoForNoti +
            " AND member_customer_id in ?1" +
            " AND status <> ?2"
            , nativeQuery = true)
    List<ContractInfoForNotiProjection> findNeededInfoForNotiByListMemberId(List<Long> ids, int canceledStatus);

    @Query(value = queryGetAllAvailableContractInfoForNoti +
            " AND customer_id = ?1" +
            " AND status <> ?2"
            , nativeQuery = true)
    List<ContractInfoForNotiProjection> findNeededInfoForNotiByCustomerId(Long id, int canceledStatus);

    @Query(value = "SELECT * FROM contract WHERE prefix_code = ?1" +
            " AND contract_type_id = ?2" +
            " GROUP BY prefix_code" +
            " HAVING min(suffix_code)", nativeQuery = true)
    Contract findContractClone(String prefixCode, Long contractTypeId);

    @Query(value = "SELECT * FROM contract WHERE contract_type_id like ?1" +
            " AND is_deleted = false AND status <> 1" +
            " AND vehicle_id IS NOT NULL ORDER BY created_at DESC", nativeQuery = true)
    List<Contract> findAllByContractTypeAndAssignedVehicle(String typeId);

    @Query(value = "SELECT c.id," +
            "       MAX(jdd.date)      jdd_max_date," +
            "       MAX(jd.time_start) jd_time_start " +
            "FROM contract c" +
            "         LEFT JOIN journey_diary_daily jdd ON c.id = jdd.contract_id AND jdd.is_deleted = FALSE" +
            "         LEFT JOIN journey_diary jd ON c.id = jd.contract_id AND jd.step != 4 AND jd.is_deleted = FALSE " +
            "WHERE c.contract_type_id = 1" +
            "  AND (c.date_early_termination IS NULL OR c.from_datetime <= c.date_early_termination)" +
            "  AND c.from_datetime <= :date" +
            "  AND c.is_deleted = FALSE " +
            "GROUP BY c.id, c.to_datetime, c.date_early_termination " +
            "HAVING jdd_max_date IS NULL" +
            "    OR (jdd_max_date < :date AND" +
            "        jdd_max_date < c.to_datetime AND" +
            "        (c.date_early_termination IS NULL OR" +
            "         jdd_max_date <= DATE_SUB(c.date_early_termination, INTERVAL 1 DAY)) AND" +
            "        (jd_time_start IS NULL OR" +
            "         jdd_max_date <= DATE_SUB(jd_time_start, INTERVAL 2 DAY))" +
            "    )" +
            "ORDER BY c.id DESC", nativeQuery = true)
    List<Object[]> findContractsWithDriverMissingJourneyDiaryDaily(Timestamp date);

    @Query(value = "SELECT c.id, " +
            "       MAX(jdd.date)             max_jdd_date, " +
            "       MAX(jdd.journey_diary_id) max_jd_id, " +
            "       MAX(fjd.id)               max_fjd_id, " +
            "       MAX(njd.time_start)       jd_time_start " +
            "FROM contract c " +
            "         LEFT JOIN journey_diary_daily jdd ON c.id = jdd.contract_id AND jdd.is_deleted = FALSE " +
            "         LEFT JOIN journey_diary fjd ON c.id = fjd.contract_id AND fjd.step = 4 AND fjd.is_deleted = FALSE " +
            "         LEFT JOIN journey_diary njd ON c.id = njd.contract_id AND njd.step != 4 AND njd.is_deleted = FALSE " +
            "WHERE c.contract_type_id = 2 " +
            "  AND (c.date_early_termination IS NULL OR c.from_datetime <= c.date_early_termination) " +
            "  AND c.from_datetime <= :date " +
            "  AND c.is_deleted = FALSE " +
            "GROUP BY c.id, c.to_datetime, c.date_early_termination " +
            "HAVING max_jdd_date IS NULL " +
            "    OR max_jd_id != max_fjd_id " +
            "    OR (max_jdd_date < :date AND " +
            "        max_jdd_date < c.to_datetime AND " +
            "        (c.date_early_termination IS NULL OR " +
            "         max_jdd_date <= DATE_SUB(c.date_early_termination, INTERVAL 1 DAY)) AND " +
            "        (jd_time_start IS NULL OR " +
            "         max_jdd_date <= DATE_SUB(jd_time_start, INTERVAL 2 DAY)) " +
            "    ) " +
            "ORDER BY c.id DESC", nativeQuery = true)
    List<Object[]> findContractsWithoutDriverMissingJourneyDiaryDaily(Timestamp date);

}
