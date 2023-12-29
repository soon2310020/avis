package com.stg.repository;

import com.stg.entity.Campaign;
import com.stg.entity.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    String SELECT_ALL_CAMPAIGN = "SELECT u.* FROM campaign u ";
    String COUNT_ALL_CAMPAIGN = "SELECT count(*) FROM campaign u ";
    String FILTER_QUERY_CAMPAIGN = "where (:query = '' OR (u.name ILIKE CONCAT('%', :query, '%'))) ";

    String FILTER_BY_STATUS = "AND CASE " +
            "WHEN :status = '' THEN 1=1 " +
            "WHEN :status = 'NOT_STARTED' THEN (CURRENT_DATE < TO_DATE(cast(u.start_time as TEXT),'YYYY-MM-DD')) " +
            "WHEN :status = 'IN_PROGRESS' THEN (CURRENT_DATE > TO_DATE(cast(u.start_time as TEXT),'YYYY-MM-DD') and CURRENT_DATE < TO_DATE(cast(u.end_time as TEXT),'YYYY-MM-DD')) " +
            "WHEN :status = 'ENDED' THEN (CURRENT_DATE > TO_DATE(cast(u.end_time as TEXT),'YYYY-MM-DD')) " +
            "END ";

    String ORDER_BY_CAMPAIGN_ID = "ORDER BY u.creation_time DESC ";

    @Query(value = SELECT_ALL_CAMPAIGN + FILTER_QUERY_CAMPAIGN + FILTER_BY_STATUS + ORDER_BY_CAMPAIGN_ID +
            "OFFSET (:page - 1) * :limit LIMIT :limit", nativeQuery = true)
    List<Campaign> listCampaignPaging (@Param("page") int page, @Param("limit") int limit,
                                       @Param("query") String query, @Param("status") String status);

    @Query(value = COUNT_ALL_CAMPAIGN + FILTER_QUERY_CAMPAIGN + FILTER_BY_STATUS, nativeQuery = true)
    long totalCampaign(@Param("query") String query, @Param("status") String status);

    @Query(value = SELECT_ALL_CAMPAIGN + FILTER_QUERY_CAMPAIGN + FILTER_BY_STATUS + ORDER_BY_CAMPAIGN_ID, nativeQuery = true)
    List<Campaign> listCampaignNoPaging (@Param("query") String query, @Param("status") String status);

}
