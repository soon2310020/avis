package com.stg.repository;

import com.stg.entity.address.AddressWard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressWardRepository extends JpaRepository<AddressWard, Long> {
    @Query(value = "SELECT aw.* FROM address_ward aw JOIN address_district ad ON aw.district_code = ad.code " +
            "WHERE (?1 IS NULL OR ad.code ILIKE CONCAT('%', ?1, '%')) " +
            "AND (?2 IS NULL OR aw.code ILIKE CONCAT('%', ?2, '%')) " +
            "AND (?3 IS NULL OR aw.name ILIKE CONCAT('%', ?3, '%')) ORDER BY aw.name ", nativeQuery = true)
    List<AddressWard> findByDistrictAndCodeAndName(String districtCode,
                                                      String wardCode,
                                                      String wardNam);

}
