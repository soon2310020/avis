package com.stg.repository;

import com.stg.entity.InsurancePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsurancePackageRepository extends JpaRepository<InsurancePackage, Integer> {

    @Query(value = "SELECT ip.* FROM insurance_package ip " +
            "where ((:query = '' OR ip.package_name ILIKE CONCAT('%', :query, '%'))) " +
            "and (:category = '' or category = :category) " +
            "and (:packageName = '' or package_name = :packageName) " +
            "ORDER BY ip.id ASC OFFSET (:page - 1) * :size LIMIT :size", nativeQuery = true)
    List<InsurancePackage> listInsurancePackage(@Param("page") int page,
                                                @Param("size") int size,
                                                @Param("query") String query,
                                                @Param("category") String category,
                                                @Param("packageName") String packageName);

    @Query(value = "Select count (ip.id) FROM insurance_package ip " +
            "where ((:query = '' OR ip.package_name ILIKE CONCAT('%', :query, '%'))) " +
            "and (:category = '' or category = :category) " +
            "and (:packageName = '' or package_name = :packageName) ", nativeQuery = true)
    long totalInsurancePackage(@Param("query") String query,
                               @Param("category") String category,
                               @Param("packageName") String packageName);

    @Query(value = "SELECT ip.* FROM insurance_package ip " +
            "where ((:query = '' OR ip.package_name ILIKE CONCAT('%', :query, '%'))) " +
            "and (:category = '' or category = :category) " +
            "and (:packageName = '' or package_name = :packageName) " +
            "ORDER BY ip.id ASC", nativeQuery = true)
    List<InsurancePackage> listNoPaging (@Param("query") String query,
                                         @Param("category") String category,
                                         @Param("packageName") String packageName);

    List<InsurancePackage> findByType(String type);

    List<InsurancePackage> findByPackageName(String packageName);

}
