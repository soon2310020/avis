package com.stg.repository;

import com.stg.entity.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    String SELECT_ALL_CUSTOMER = "SELECT u.*, s.gain FROM customer u " +
            "left join segment s on u.segment_id = s.id ";
    String COUNT_ALL_CUSTOMER = "SELECT count(*) FROM customer u " +
            "left join segment s on u.segment_id = s.id ";
    String FILTER_QUERY_CUSTOMER = "where (:query = '' OR (u.full_name ILIKE CONCAT('%', :query, '%')) " +
            "or (u.full_name_t24 ILIKE CONCAT('%', :query, '%')) " +
            "or (u.mb_id ILIKE CONCAT('%', :query, '%'))) " +
            "and (:segment = '' OR (s.gain ILIKE CONCAT('%', :segment, '%'))) ";

    String ORDER_BY_CUSTOMER_ID = "ORDER BY u.id DESC ";

    @Query(value = SELECT_ALL_CUSTOMER + FILTER_QUERY_CUSTOMER + ORDER_BY_CUSTOMER_ID +
            "OFFSET (:page - 1) * :limit LIMIT :limit", nativeQuery = true)
    List<Customer> listCustomer(@Param("page") int page,
                                 @Param("limit") int limit,
                                 @Param("query") String query,
                                 @Param("segment") String segment);

    @Query(value = COUNT_ALL_CUSTOMER + FILTER_QUERY_CUSTOMER, nativeQuery = true)
    long totalCustomer(@Param("query") String query, @Param("segment") String segment);

    @Query(value = SELECT_ALL_CUSTOMER + FILTER_QUERY_CUSTOMER + ORDER_BY_CUSTOMER_ID, nativeQuery = true)
    List<Customer> listAllPageCustomers (@Param("query") String query, @Param("segment") String segment);

    Customer findByMbId(@Param("mbId") String mbId);

    @Query(value = "SELECT u.* FROM customer u JOIN insurance_payment ip ON ip.customer_id = u.id " +
            "WHERE ip.id = ?1", nativeQuery = true)
    Customer findByInsurancePaymentId(long insurancePaymentId);

    @Modifying
    @Query("update Customer c set c.managingUnit = ?1 where c.identification = ?2")
    void updateManagingUnitByIdentity(String managingUnit, String identityNumb);
}
