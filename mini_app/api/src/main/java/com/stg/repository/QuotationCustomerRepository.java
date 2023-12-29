package com.stg.repository;

import com.stg.entity.quotation.QuotationCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotationCustomerRepository extends JpaRepository<QuotationCustomer, Long> {


}
