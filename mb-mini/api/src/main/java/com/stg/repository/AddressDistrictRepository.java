package com.stg.repository;

import com.stg.entity.address.AddressDistrict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressDistrictRepository extends JpaRepository<AddressDistrict, Long> {
    @Query(value = "SELECT ad.* FROM address_district ad JOIN address_province ap ON ad.province_code = ap.code " +
            "WHERE (?1 IS NULL OR ap.code ILIKE CONCAT('%', ?1, '%')) " +
            "AND (?2 IS NULL OR ad.code ILIKE CONCAT('%', ?2, '%')) " +
            "AND (?3 IS NULL OR ad.name ILIKE CONCAT('%', ?3, '%')) ORDER BY ad.name ", nativeQuery = true)
    List<AddressDistrict> findByProvinceAndCodeAndName(String provinceCode,
                                                       String districtCode,
                                                       String districtName);
}