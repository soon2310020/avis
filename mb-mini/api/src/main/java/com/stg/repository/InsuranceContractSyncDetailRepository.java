package com.stg.repository;

import com.stg.entity.InsuranceContractSyncDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface InsuranceContractSyncDetailRepository extends JpaRepository<InsuranceContractSyncDetail, Long> {
    @Query(value = "from InsuranceContractSyncDetail icsd " +
            "join InsuranceContractSync ics on ics.id = icsd.insuranceContractSync.id " +
            "where " +
            "ics.id = :contractSyncId " +
            "and icsd.dueFromDate = :dueFromDate and icsd.dueToDate = :dueToDate ")
    InsuranceContractSyncDetail getCurrentContractSyncDetail(Long contractSyncId, LocalDate dueFromDate, LocalDate dueToDate);

    @Query(value = "from InsuranceContractSyncDetail icsd " +
            "join InsuranceContractSync ics on ics.id = icsd.insuranceContractSync.id " +
            "where " +
            "ics.mbalPolicyNumber = :mbalPolicyNumber order by ics.id desc ")
    List<InsuranceContractSyncDetail> findAllByMbalPolicyNumber(String mbalPolicyNumber);

    @Query(value = "select * from insurance_contract_sync_detail icsd " +
            "join insurance_contract_sync ics on ics.id = icsd.insurance_contract_sync_id " +
            "where ics.mbal_policy_number  = :mbalPolicyNumber " +
            "and icsd.premium_type in ('NEXTDUE', 'DUE') " +
            "and (CURRENT_DATE between ((icsd.due_from_date) - INTERVAL '60 DAY') and icsd.due_from_date) " +
            "order by icsd.due_from_date limit 1", nativeQuery = true)
    InsuranceContractSyncDetail getNearestContractSyncDetail(String mbalPolicyNumber);
}
