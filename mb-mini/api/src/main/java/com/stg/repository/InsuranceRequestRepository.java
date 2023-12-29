package com.stg.repository;

import com.stg.entity.InsuranceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface InsuranceRequestRepository extends JpaRepository<InsuranceRequest, Long> {
    String FILTER_INSURANCE_REQUEST = "left join insurance_package ip on ip.id = ir.insurance_package_id " +
            "join customer c on c.id = ir.customer_id " +
            "left join segment s on c.segment_id = s.id " +
            "where (:query = '' OR (ip.package_name ILIKE CONCAT('%', :query, '%')) " +
            "or (c.full_name ILIKE CONCAT('%', :query, '%')) " +
            "or (c.mb_id ILIKE CONCAT('%', :query, '%'))) " +
            "and (:segment = '' OR (s.gain ILIKE CONCAT('%', :segment, '%'))) " +
            "and (ir.status in (:listStatus)) " +
            "and (:category = '' or ip.category = :category) " +
            "and case " +
            "   when :filterUrStyle = 'Yes' then (ir.insurance_package_id in (:filterIdPackage) or ir.insurance_package_id is null) " +
            "   when :filterUrStyle = 'No' then (ir.insurance_package_id in (:filterIdPackage)) " +
            "   when :filterUrStyle = '' then 1=1 " +
            "end " ;

    @Query(value = "SELECT ir.* FROM insurance_request ir " +
            FILTER_INSURANCE_REQUEST +
            "ORDER BY ir.id DESC OFFSET (:page - 1) * :size LIMIT :size", nativeQuery = true)
    List<InsuranceRequest> listInsuranceRequest(@Param("page") int page,
                                                @Param("size") int size,
                                                @Param("query") String query,
                                                @Param("segment") String segment,
                                                @Param("listStatus") List<Boolean> listStatus,
                                                @Param("category") String category,
                                                @Param("filterUrStyle") String filterUrStyle,
                                                @Param("filterIdPackage") Set<Integer> filterIdPackage);

    @Query(value = "Select count (ir.id) FROM insurance_request ir " + FILTER_INSURANCE_REQUEST, nativeQuery = true)
    long totalInsuranceRequest(@Param("query") String query,
                               @Param("segment") String segment,
                               @Param("listStatus") List<Boolean> listStatus,
                               @Param("category") String category,
                               @Param("filterUrStyle") String filterUrStyle,
                               @Param("filterIdPackage") Set<Integer> filterIdPackage);

    @Query(value = "SELECT ir.* FROM insurance_request ir " + FILTER_INSURANCE_REQUEST + "ORDER BY ir.id ASC", nativeQuery = true)
    List<InsuranceRequest> findInsuranceRequestByQuery(@Param("query") String query, @Param("segment") String segment,
                                                       @Param("listStatus") List<Boolean> listStatus,
                                                       @Param("category") String category,
                                                       @Param("filterUrStyle") String filterUrStyle,
                                                       @Param("filterIdPackage") Set<Integer> filterIdPackage);

    @Query(value = "SELECT count(*) FROM insurance_request " +
            "WHERE date_trunc('week',creation_time) = date_trunc('week',CURRENT_TIMESTAMP); ", nativeQuery = true)
    long totalInsuranceRequestCurrentWeek();

    @Query(value = "SELECT count(*) FROM insurance_request " +
            "WHERE date_trunc('day',creation_time) = date_trunc('day', CURRENT_TIMESTAMP); ", nativeQuery = true)
    long totalInsuranceRequestToday();
}
