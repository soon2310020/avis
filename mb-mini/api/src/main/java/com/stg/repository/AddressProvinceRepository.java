package com.stg.repository;

import com.stg.entity.address.AddressProvince;
import com.stg.service.dto.address.AddressDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.ConstructorResult;
import javax.persistence.SqlResultSetMapping;
import java.util.List;
import java.util.Optional;

@Repository
public interface AddressProvinceRepository extends JpaRepository<AddressProvince, Long> {

    @Query(value = "SELECT ap.* FROM address_province ap " +
            "WHERE (?1 IS NULL OR ap.code ILIKE CONCAT('%', ?1, '%')) " +
            "AND (?2 IS NULL OR ap.name ILIKE CONCAT('%', ?2, '%')) ORDER BY ap.name ", nativeQuery = true)
    List<AddressProvince> findByCodeAndName(String provinceCode,
                                            String provinceName);

    @Query(value = "select ap.code  provinceCode," +
            "       ap.name provinceName," +
            "       ad.code   districtCode, " +
            "       ad.name districtName, " +
            "       aw.code   wardCode, " +
            "       aw.name wardName " +
            " from address_ward aw " +
            " inner join address_district ad on " +
            " ad.code = aw.district_code " +
            " inner join address_province ap on " +
            " ap.code = ad.province_code " +
            "where (?1 = '' or ?1 ilike '%' || ad.unsigned_name || '%') " +
            "  and (?2 = '' or ?2 ilike '%' || ap.unsigned_name || '%') " +
            "  and (?3 = '' or ?3 ilike '%' || aw.unsigned_name || '%')  limit 1 ",nativeQuery = true)
    Optional<AddressDTO> findByUnsignedName(String districtName, String provinceName, String wardName);

    Optional<AddressProvince> findByCode(String code);
}
