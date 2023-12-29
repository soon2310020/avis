package com.stg.repository;

import com.stg.entity.PrimaryProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface PrimaryProductRepository extends JpaRepository<PrimaryProduct, Long> {
    @Query(value = "select * from primary_product pi where insurance_request_id = :insuranceRequestId limit 1 ", nativeQuery = true)
    PrimaryProduct findByInsuranceRequestId(@Param("insuranceRequestId") Long insuranceRequestId);

    @Query(value = "select * from primary_product pi where insurance_request_id in (:requestIds)", nativeQuery = true)
    List<PrimaryProduct> findPrimaryProductByRequestIds(@Param("requestIds") List<Long> requestIds);

    @Query(value = "select pp.* " +
            "from primary_product pp " +
            "join insurance_payment ip on pp.insurance_request_id = ip.insurance_request_id " +
            "join insurance_contract ic on ic.transaction_id = ip.transaction_id  " +
            "where ic.id in (:contractIds)", nativeQuery = true)
    List<PrimaryProduct> findPrimaryProductByContractIds(@Param("contractIds") List<Long> contractIds);
}
