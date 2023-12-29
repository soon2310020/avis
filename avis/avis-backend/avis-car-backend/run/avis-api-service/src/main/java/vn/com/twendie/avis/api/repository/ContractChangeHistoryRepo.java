package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.com.twendie.avis.data.model.ContractChangeHistory;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

public interface ContractChangeHistoryRepo extends JpaRepository<ContractChangeHistory, Long> {

    List<ContractChangeHistory> findAllByMappingFieldCodeFontendFieldNameInAndFromDateBetweenAndContractDeletedFalseAndDeletedFalseOrderByCreatedAt(
            Collection<String> fieldNames, Timestamp effectiveDateFrom, Timestamp effectiveDateTo);

    @Query(value = "SELECT *, ROW_NUMBER() OVER (PARTITION BY cch.mapping_field_code_fontend_id order by cch.from_date)" +
            " FROM contract_change_history cch" +
            " JOIN user u on cch.created_by_id = u.id" +
            " JOIN contract c on cch.contract_id = c.id" +
            " JOIN mapping_field_code_fontend mfcf on cch.mapping_field_code_fontend_id = mfcf.id" +
            " WHERE cch.contract_id = ?1 AND cch.is_deleted = false",
            nativeQuery = true)
    List<ContractChangeHistory> findAllByContractIdAndDeletedFalse(Long id);

    @Query(value = "(SELECT new_value FROM contract_change_history" +
            " WHERE contract_id = ?1 AND mapping_field_code_fontend_id = ?2 AND from_date < ?3" +
            " AND is_deleted = false ORDER BY from_date DESC, id DESC LIMIT 1)" +
            " UNION" +
            " (SELECT old_value FROM contract_change_history" +
            " WHERE contract_id = ?1 AND mapping_field_code_fontend_id = ?2 AND (from_date > ?3 or from_date = ?3)" +
            " AND is_deleted = false ORDER BY from_date, id LIMIT 1)", nativeQuery = true)
    List<String> findClosetChangeOfField(Long contractId, Long fieldId, Timestamp timestamp);

    List<ContractChangeHistory> findAllByContractIdIn(Collection<Long> contractIds);
            
}
