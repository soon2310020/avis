package com.stg.repository;

import com.stg.entity.InsuranceContractSync;
import com.stg.service.dto.insurance.FilterContractParam;
import com.stg.service.dto.insurance.InsuranceContractVo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InsuranceContractSyncRepository extends JpaRepository<InsuranceContractSync, Long> {

    String SELECT_INSURANCE_CONTRACT = "select ics.id as id, " +
            "ics.mbal_policy_number as mbalPolicyNumber," +
            "ics.policy_status as status, " +
            "ics.periodic_prem as periodicPrem, " +
            "ics.pay_frequency as mbalPeriodicFeePaymentTime, " +
            "ics.policy_eff_date as createdDate," +
            "ics.ph_name as phName," +
            "t.min_top_up as minTopUp," +
            "t.due_from_date as dueFromDate," +
            "t.due_to_date as dueToDate," +
            "t.premium_type as premiumType," +
            "t.due_amount as dueAmount," +
            "ics.prd_name as mbalProductName ";

    String COUNT_INSURANCE_CONTRACT = "select count(ics.id) ";

    String FILTER_INSURANCE_CONTRACT = " from insurance_contract_sync ics " +
            "join customer c on c.id = ics.customer_id " +
            "left join (select * from insurance_contract_sync_detail icsd where icsd.premium_type in ('NEXTDUE', 'DUE') " +
            "and (CURRENT_DATE between ((icsd.due_from_date) - INTERVAL '60 DAY') and icsd.due_from_date)) t on t.insurance_contract_sync_id = ics.id ";

    String WHERE = " where c.mb_id = :#{#filterParam.mbId} and " +
            "  (:#{#filterParam.query} = '' " +
            "    or (c.full_name ILIKE CONCAT('%', :#{#filterParam.query}, '%'))  " +
            "    or (ics.mbal_policy_number ILIKE CONCAT('%', :#{#filterParam.query}, '%'))    " +
            "    or (ics.prd_name ILIKE CONCAT('%', :#{#filterParam.query}, '%'))) ";

    InsuranceContractSync findInsuranceContractSyncsByMbalPolicyNumber(String mbalPolicyNumber);

    @Query(value = SELECT_INSURANCE_CONTRACT + FILTER_INSURANCE_CONTRACT +
            "where c.mb_id = :#{#filterParam.mbId} " +
            "and (:#{#filterParam.query} = '' " +
            "    or (c.full_name ILIKE CONCAT('%', :#{#filterParam.query}, '%'))  " +
            "    or (ics.mbal_policy_number ILIKE CONCAT('%', :#{#filterParam.query}, '%'))    " +
            "    or (ics.prd_name ILIKE CONCAT('%', :#{#filterParam.query}, '%'))) " +
            "and (:#{#filterParam.status.getText()} = '' or ics.policy_status = :#{#filterParam.status.getText()}) " +
            "ORDER BY ics.id DESC OFFSET (:page) * :size LIMIT :size", nativeQuery = true)
    List<InsuranceContractVo> listInsuranceContract(@Param("filterParam") FilterContractParam filterParam,
                                                    @Param("page") int page,
                                                    @Param("size") int size);

    @Query(value = COUNT_INSURANCE_CONTRACT + FILTER_INSURANCE_CONTRACT +
            "where c.mb_id = :#{#filterParam.mbId} " +
            "and " +
            "  (:#{#filterParam.query} = '' " +
            "    or (c.full_name ILIKE CONCAT('%', :#{#filterParam.query}, '%')) " +
            "    or (ics.mbal_policy_number ILIKE CONCAT('%', :#{#filterParam.query}, '%')) " +
            "    or (ics.prd_name ILIKE CONCAT('%', :#{#filterParam.query}, '%'))) " , nativeQuery = true)
    Long total(@Param("filterParam") FilterContractParam filterParam);

    InsuranceContractSync findInsuranceContractSyncsByMbalAppNo(String mbalAppNo);

}
