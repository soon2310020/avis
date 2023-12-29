package com.stg.repository;

import com.stg.entity.IllustrationTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface IllustrationTableRepository extends JpaRepository<IllustrationTable, String> {
    String FILTER_ILLUSTRATION_TABLE = "join customer c on c.id = it.customer_id " +
            "left join insurance_package ip on ip.id = it.insurance_package_id " +
            "left join segment s on c.segment_id = s.id " +
            "where (:query = '' " +
            "   OR (it.illustration_number ILIKE CONCAT('%', :query, '%')) " +
            "   OR (c.full_name ILIKE CONCAT('%', :query, '%')) " +
            "   OR case " +
            "       when :searchUrStyle = 'Yes' then (it.insurance_package_id in (:searchIdPackage) or it.insurance_package_id is null) " +
            "       when :searchUrStyle = 'No' then (it.insurance_package_id in (:searchIdPackage)) " +
            "end " +
            ") " +
            "and (:segment = '' OR (s.gain ILIKE CONCAT('%', :segment, '%'))) " +
            "and case " +
            "   when :filterUrStyle = 'Yes' then (it.insurance_package_id in (:filterIdPackage) or it.insurance_package_id is null) " +
            "   when :filterUrStyle = 'No' then (it.insurance_package_id in (:filterIdPackage)) " +
            "   when :filterUrStyle = '' then 1=1 " +
            "end " +
            "and (:category = '' or category = :category) " ;

    @Query(value = "SELECT it.* FROM illustration_table it " + FILTER_ILLUSTRATION_TABLE +
            "ORDER BY it.creation_time DESC OFFSET (:page - 1) * :size LIMIT :size", nativeQuery = true)
    List<IllustrationTable> listIllustrationTable(@Param("page") int page,
                                                  @Param("size") int size,
                                                  @Param("query") String query,
                                                  @Param("segment") String segment,
                                                  @Param("searchIdPackage") Set<Integer> searchIdPackage,
                                                  @Param("searchUrStyle") String searchUrStyle,
                                                  @Param("filterIdPackage") Set<Integer> filterIdPackage,
                                                  @Param("filterUrStyle") String filterUrStyle,
                                                  @Param("category") String category);

    @Query(value = "Select count (it.illustration_number) FROM illustration_table it " + FILTER_ILLUSTRATION_TABLE, nativeQuery = true)
    long totalIllustrationTable(@Param("query") String query,
                                @Param("segment") String segment,
                                @Param("searchIdPackage") Set<Integer> searchIdPackage,
                                @Param("searchUrStyle") String searchUrStyle,
                                @Param("filterIdPackage") Set<Integer> filterIdPackage,
                                @Param("filterUrStyle") String filterUrStyle,
                                @Param("category") String category);


    @Query(value = "SELECT it.* FROM illustration_table it " + FILTER_ILLUSTRATION_TABLE +
            "ORDER BY it.illustration_number ASC ", nativeQuery = true)
    List<IllustrationTable> listIllustrationTableNoPaging(@Param("query") String query,
                                                          @Param("segment") String segment,
                                                          @Param("searchIdPackage") Set<Integer> searchIdPackage,
                                                          @Param("searchUrStyle") String searchUrStyle,
                                                          @Param("filterIdPackage") Set<Integer> filterIdPackage,
                                                          @Param("filterUrStyle") String filterUrStyle,
                                                          @Param("category") String category);

    @Query(value = "SELECT count(*) FROM illustration_table " +
            "WHERE date_trunc('week',creation_time) = date_trunc('week',CURRENT_TIMESTAMP); ", nativeQuery = true)
    long totalIllustrationCurrentWeek();

    @Query(value = "SELECT count(*) FROM illustration_table " +
            "WHERE date_trunc('week',creation_time) = date_trunc('week',CURRENT_TIMESTAMP) " +
            "AND extract(isodow from creation_time) = :dayNum ", nativeQuery = true)
    Long totalIllustrationCurrentWeekByDayOfWeek(@Param("dayNum") long dayNum);

    @Query(value = "select ir.id from illustration_table it " +
            "join insurance_request ir on it.illustration_number = ir.illustration_number " +
            "where it.illustration_number = :illustrationNumber ", nativeQuery = true)
    Long findInsuranceRequestIdByIllustrationTable(@Param("illustrationNumber") String illustrationNumber);
}
