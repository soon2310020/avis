package com.stg.repository;

import com.stg.entity.ProductMbal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductMbalRepository extends JpaRepository<ProductMbal, Long> {
    @Query(value = "select * from product_mbal pm " +
            "where pm.insurance_contract_id = :insuranceContractId and main = :main", nativeQuery = true)
    List<ProductMbal> findByInsuranceContractId(@Param("insuranceContractId") Long insuranceContractId,
                                                @Param("main") boolean main);

    @Query(value = "select * from product_mbal pm " +
            "where pm.insurance_contract_id in (:insuranceContractIds) and main = :main", nativeQuery = true)
    List<ProductMbal> findByInsuranceContractIdListAndByMain(@Param("insuranceContractIds") List<Long> insuranceContractIds,
                                                @Param("main") boolean main);

    @Query(value = "select * from product_mbal pm where pm.product_code = :productCode and pm.insurance_contract_id = :insuranceContractId limit 1 ", nativeQuery = true)
    ProductMbal findByProductCode(@Param("productCode") String productCode, @Param("insuranceContractId") Long insuranceContractId);

}
