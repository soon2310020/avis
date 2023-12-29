package vn.com.twendie.avis.mobile.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.mobile.api.model.projection.ContractDetail;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface ContractRepo extends JpaRepository<Contract, Long> {

    String getLastedCreatedAt = "SELECT MAX(id) FROM contract_driver_history cd" +
            " WHERE is_deleted = false" +
            " AND cd.user_id = ?1 GROUP BY cd.contract_id HAVING MAX(created_at)";

    String getLastedCreatedAtDriverContractHistory = "SELECT user_id, contract_id, status, created_at " +
            "FROM contract_driver_history cd WHERE cd.id IN (" +
            getLastedCreatedAt + ")";

    String LeftJoinWithLatestDriverContractHistory = " LEFT JOIN (" + getLastedCreatedAtDriverContractHistory +
            ")" + " AS cdh ON c.id = cdh.contract_id";

    String getContractsByDriverIdQueryWithDriverContractStatus = "SELECT c.id AS id, c.code AS code, c.status AS status, c.from_datetime AS fromDateTime, c.to_datetime AS toDateTime, c.driver_is_transferred_another AS driverLend, c.vehicle_is_transferred_another AS vehicleLend," +
            " c2.name AS customerName, c2.mobile AS mobile, c2.address AS address, c2.country_code AS countryCode, v.type AS vehicleType, v.color AS vehicleColor, v.number_seat AS vehicleNumberSeat," +
            " v.number_plate AS numberPlate, cpt.name AS periodTypeName, cdh.status AS driverContractStatus, cdh.created_at AS historyCreatedAt," +
            " mc.name AS memberName, mc.mobile AS memberMobile, mc.country_code AS memberCountryCode FROM contract c" +
            " LEFT JOIN customer c2 ON c.customer_id = c2.id" +
            " LEFT JOIN vehicle v ON c.vehicle_id = v.id" +
            " LEFT JOIN contract_period_type cpt ON c.contract_period_type_id = cpt.id" +
            " LEFT JOIN member_customer mc on c.member_customer_id = mc.id";

    @Query(value = "SELECT * FROM contract WHERE driver_id = :userId and from_datetime <= CURRENT_TIMESTAMP and to_datetime >= CURRENT_TIMESTAMP and is_deleted = false limit 1", nativeQuery = true)
    Contract findCurrentContractByUserId(@Param("userId") Long userId);

    @Query(value = getContractsByDriverIdQueryWithDriverContractStatus +
            // this query use 1st param as user_id of contract_driver_history table
            LeftJoinWithLatestDriverContractHistory +
            " WHERE c.is_deleted = false" +
            " AND (c.driver_id = ?1 OR (cdh.user_id = ?1 AND cdh.created_at > c.from_datetime))" +
            " AND (?2 is null OR ?2 BETWEEN c.from_datetime AND c.to_datetime)" +
            " ORDER BY c.created_at DESC",
            countProjection = "c.id",
            nativeQuery = true)
    Page<ContractDetail> findAllByDriverId(Long driverId, Timestamp timestamp, Pageable pageable);

    @Query(value = getContractsByDriverIdQueryWithDriverContractStatus +
            // this query use 1st param as user_id of contract_driver_history table
            LeftJoinWithLatestDriverContractHistory +
            " WHERE c.is_deleted = false" +
            " AND c.driver_id = ?1" +
            " AND c.status = ?2" +
            " AND cdh.status <> ?3" +
            " ORDER BY c.created_at DESC",
            countProjection = "c.id",
            nativeQuery = true)
    Page<ContractDetail> findInProgressContracts(Long driverId, Integer inProgressCode, Integer historyCode, Pageable pageable);

    @Query(value = getContractsByDriverIdQueryWithDriverContractStatus +
            // this query use 1st param as user_id of contract_driver_history table
            LeftJoinWithLatestDriverContractHistory +
            " WHERE c.is_deleted = false" +
            " AND c.driver_id = ?1" +
            " AND c.status = ?2" +
            " AND cdh.status <> 0" +
            " ORDER BY c.created_at DESC",
            countProjection = "id",
            nativeQuery = true)
    List<ContractDetail> findUpComingContracts(Long driverId, Integer code);

    @Query(value = getContractsByDriverIdQueryWithDriverContractStatus +
            // this query use 1st param as user_id of contract_driver_history table
            LeftJoinWithLatestDriverContractHistory +
            " WHERE c.is_deleted = false" +
            " AND (c.driver_id = ?1 OR cdh.user_id = ?1)" +
            " AND TIMESTAMP(DATE_ADD(c.to_datetime,INTERVAL 1 day )) <= ?2" +
            " ORDER BY c.created_at DESC",
            nativeQuery = true)
    List<ContractDetail> findFinishedContracts(Long driverId, String dateTime);

    @Query(value = getContractsByDriverIdQueryWithDriverContractStatus +
            // this query use 1st param as user_id of contract_driver_history table
            LeftJoinWithLatestDriverContractHistory +
            " WHERE c.is_deleted = false" +
            " AND (c.driver_id = ?1 OR cdh.user_id = ?1)" +
            " AND c.status = ?2" +
            " ORDER BY c.created_at DESC",
            nativeQuery = true)
    List<ContractDetail> findAllByDriverIdFilterByStatus(Long driverId, int status);

    @Query(value = getContractsByDriverIdQueryWithDriverContractStatus +
            // this query use 1st param as user_id of contract_driver_history table
            LeftJoinWithLatestDriverContractHistory +
            " WHERE c.is_deleted = false" +
            " AND (c.driver_id = ?1 OR cdh.user_id = ?1)" +
            " AND (cdh.status = ?2 OR c.vehicle_is_transferred_another = true OR c.driver_is_transferred_another = true)" +
            " AND c.status <> ?3" +
            " AND TIMESTAMP(DATE_ADD(c.to_datetime,INTERVAL 1 day )) > ?4" +
            " ORDER BY c.created_at DESC",
            nativeQuery = true)
    List<ContractDetail> findAllByDriverIdFilterByDriverContractStatus(Long driverId,
                                                                       int driverContractStatus, int contractStatus, String dateTime);

    @Query(value = getContractsByDriverIdQueryWithDriverContractStatus +
            // this query use 1st param as user_id of contract_driver_history table
            LeftJoinWithLatestDriverContractHistory +
            " WHERE c.is_deleted = false" +
            " AND (c.driver_id = ?1 OR cdh.user_id = ?1)" +
            " AND c.id = ?2" +
            " ORDER BY c.created_at DESC LIMIT 1", nativeQuery = true)
    ContractDetail findByContractId(Long driverId, Long contractId);

    Optional<Contract> findByIdAndDeletedFalse(Long id);

}
