package com.stg.repository;

import com.stg.entity.AutoDebitPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AutoDebitPaymentRepository extends JpaRepository<AutoDebitPayment, Long> {
    AutoDebitPayment findBySourceNumberAndSourceType(String sourceNumber, String sourceType);

    @Query(value = "SELECT * FROM auto_debit_payment adpt " +
            "INNER JOIN insurance_payment ip on ip.auto_debit_payment_id = adpt.id " +
            "WHERE ip.auto_pay = true " +
            "and ip.id = ?1 ", nativeQuery = true)
    AutoDebitPayment findByInsurancePaymentId(long insurancePaymentId);
}
