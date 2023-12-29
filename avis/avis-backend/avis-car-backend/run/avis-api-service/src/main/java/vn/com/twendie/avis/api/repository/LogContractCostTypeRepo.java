package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.com.twendie.avis.api.model.projection.ContractCodeValue;
import vn.com.twendie.avis.api.model.projection.LogContractCostProjection;
import vn.com.twendie.avis.api.model.projection.PRLogContractCostProjection;
import vn.com.twendie.avis.data.model.LogContractPriceCostType;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

public interface LogContractCostTypeRepo extends JpaRepository<LogContractPriceCostType, Long> {

    List<LogContractPriceCostType> findAllByFromDateBetweenAndContractDeletedFalseAndDeletedFalseOrderByCreatedAt(Timestamp fromDateFrom, Timestamp fromDateTo);

    @Query(value = "SELECT l.cost_type_id AS costTypeId, l.from_date AS fromDate, l.price AS price, l.created_at as createdAt," +
            " u.name AS userName, ct.name AS costTypeName," +
            "ROW_NUMBER() OVER (PARTITION BY l.cost_type_id ORDER BY l.from_date)" +
            " FROM log_contract_price_cost_type l" +
            " LEFT JOIN cost_type ct ON l.cost_type_id = ct.id" +
            " LEFT JOIN user u on l.created_by_id = u.id" +
            " WHERE l.contract_id = ?1 AND l.is_deleted = false",
            nativeQuery = true)
    List<LogContractCostProjection> findAllByContractId(Long id);

    LogContractPriceCostType findFirstByContractIdAndCostTypeCodeAndFromDateBeforeAndDeletedFalseOrderByFromDateDescIdDesc(Long contractId, String costCode, Timestamp lastDayOfMonth);

    @Query(value = "SELECT * FROM log_contract_price_cost_type WHERE id IN (" +
            " SELECT MAX(lcpct.id) FROM log_contract_price_cost_type AS lcpct" +
            " LEFT JOIN cost_type ct on lcpct.cost_type_id = ct.id" +
            " WHERE contract_id = ?1 AND ct.code = ?2" +
            " AND from_date BETWEEN ?3 AND ?4 GROUP BY from_date" +
            " ) ORDER BY from_date DESC", nativeQuery = true)
    List<LogContractPriceCostType> findByContractAndDateWithin(Long contractId, String costCode, Timestamp from, Timestamp to);

    List<LogContractPriceCostType> findAllByContractIdIn(Collection<Long> contractIds);

    @Query(value = "SELECT lct.*, ct.code AS code " +
            "FROM (" +
            "         SELECT lct.contract_id AS contractId," +
            "                lct.cost_type_id," +
            "                lct.price AS value," +
            "                ROW_NUMBER() " +
            "                        OVER (PARTITION BY lct.contract_id, lct.cost_type_id " +
            "                            ORDER BY lct.from_date DESC, lct.id DESC) AS row_num " +
            "         FROM log_contract_price_cost_type lct " +
            "         WHERE lct.contract_id IN (:contractIds) " +
            "           AND NOT lct.is_deleted " +
            "           AND lct.from_date <= :timestamp" +
            "     ) lct " +
            "         LEFT JOIN cost_type ct ON lct.cost_type_id = ct.id " +
            "WHERE row_num = 1" +
            "  AND ct.code IN :codes",
            nativeQuery = true)
    List<ContractCodeValue> findContractCostTypesAtTime(Collection<Long> contractIds, List<String> codes, Timestamp timestamp);

    @Query(value = "(SELECT lcpct.id AS logId," +
            "        lcpct.from_date AS fromDate," +
            "        lcpct.to_date AS toDate," +
            "        lcpct.price AS price," +
            "        ct.code AS costCode" +
            " FROM log_contract_price_cost_type AS lcpct" +
            "    LEFT JOIN cost_type ct ON lcpct.cost_type_id = ct.id WHERE lcpct.id IN (" +
            " SELECT MAX(lcpct.id) FROM log_contract_price_cost_type AS lcpct" +
            " LEFT JOIN cost_type ct ON lcpct.cost_type_id = ct.id" +
            " WHERE contract_id = ?1 AND ct.code in ?4" +
            " AND from_date BETWEEN ?2 AND ?3 GROUP BY from_date, cost_type_id))" +
            " UNION" +
            " (SELECT id AS logId," +
            "        from_date AS fromDate," +
            "        to_date AS toDate," +
            "        price AS price," +
            "        code AS costCode FROM (SELECT lcpct.*, ct.code, ROW_NUMBER() OVER (PARTITION BY ct.code ORDER BY lcpct.from_date DESC, lcpct.id DESC) AS row_num" +
            " FROM log_contract_price_cost_type lcpct" +
            " LEFT JOIN cost_type ct ON lcpct.cost_type_id = ct.id" +
            " WHERE contract_id = ?1 AND ct.code IN ?4 AND lcpct.from_date < ?2) AS temp WHERE row_num = 1)" +
            " ORDER BY fromDate DESC", nativeQuery = true)
    List<PRLogContractCostProjection> findByContractIdAndWithinTimeAndCodeIn(Long contractId, Timestamp from, Timestamp to, List<String> codes);
}
