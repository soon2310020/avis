package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.com.twendie.avis.api.model.projection.LogContractNormProjection;
import vn.com.twendie.avis.data.model.LogContractNormList;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

public interface LogContractNormListRepo extends JpaRepository<LogContractNormList, Long> {

    @Query(value = "SELECT l.norm_list_id AS normListId, l.from_date AS fromDate, l.quota AS quota, l.created_at as createdAt," +
            " u.name AS userName, nl.name AS normName, nl.unit, ROW_NUMBER() OVER (PARTITION BY l.norm_list_id order by l.from_date)" +
            " FROM log_contract_norm_list l" +
            " LEFT JOIN norm_list nl ON l.norm_list_id = nl.id" +
            " LEFT JOIN user u ON l.created_by_id = u.id" +
            " WHERE l.contract_id = ?1 AND l.is_deleted = false",
            nativeQuery = true)
    List<LogContractNormProjection> findAllByContractId(Long id);

    List<LogContractNormList> findAllByFromDateBetweenAndContractDeletedFalseAndDeletedFalseOrderByCreatedAt(Timestamp fromDateFrom, Timestamp fromDateTo);

    @Query(value = "SELECT * FROM log_contract_norm_list WHERE id IN (" +
            " SELECT MAX(lcnl.id) FROM log_contract_norm_list AS lcnl" +
            " LEFT JOIN norm_list nl on lcnl.norm_list_id = nl.id" +
            " WHERE contract_id = ?1 AND nl.code = ?2" +
            " AND from_date BETWEEN ?3 AND ?4 GROUP BY from_date" +
            " ) ORDER BY from_date DESC", nativeQuery = true)
    List<LogContractNormList> findByContractAndDateWithin(Long contractId, String normListCode, Timestamp from, Timestamp to);

    LogContractNormList findFirstByContractIdAndNormListCodeAndFromDateBeforeAndDeletedFalseOrderByFromDateDescIdDesc(Long contractId, String normListCode, Timestamp to);

    List<LogContractNormList> findAllByContractIdIn(Collection<Long> contractIds);

}
