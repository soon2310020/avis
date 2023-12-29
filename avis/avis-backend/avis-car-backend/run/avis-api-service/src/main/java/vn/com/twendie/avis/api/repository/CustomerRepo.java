package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.twendie.avis.data.model.Customer;
import vn.com.twendie.avis.data.model.UserRole;

import java.util.List;
import java.util.Optional;

public interface CustomerRepo extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    Boolean existsByMobileAndCountryCodeAndDeletedFalse(String phoneNumber, String countryCode);

    @Query(nativeQuery = true, value = "select  count(*) from customer cus inner join user u on cus.user_id = u.id " +
            "where cus.mobile = :mobile and cus.country_code = :countryCode and cus.is_deleted = false and u.user_role_id != :userRoleId")
    int existsByMobileAndCountryCodeAndDeletedFalseAndUserIdNot(@Param("mobile") String phoneNumber,
                                                                @Param("countryCode") String countryCode,
                                                                @Param("userRoleId") Long userRoleId);


    Boolean existsByEmailAndDeletedFalseAndIdNot(String phoneNumber, Long id);


//    Boolean existsByMobileAndCountryCodeAndDeletedFalseAndUserRoleNot(String phoneNumber, String countryCode, UserRole userRole);

    Boolean existsByMobileAndCountryCodeAndIdNotAndDeletedFalse(String phoneNumber, String countryCode, Long id);

    Boolean existsByTaxCodeAndIdNotAndDeletedFalse(String taxCode, Long id);

    Boolean existsByNameIgnoreCaseAndIdNotAndCustomerTypeIdAndDeletedFalse(String name, Long customerId, Long typeId);

    Boolean existsByIdCardAndCardTypeAndIdNotAndDeletedFalse(String idCard, Integer cardType, Long id);

    Optional<Customer> findByIdAndDeletedFalse(Long id);

    Optional<Customer> findByIdAndCustomerTypeId(Long customerId, Long typeId);

    @Query(value = "SELECT MAX(code) FROM customer WHERE code LIKE CONCAT(?1, '%')", nativeQuery = true)
    String getNewestCodeByPrefix(String prefix);

    Boolean existsByCodeAndIdNot(String code, Long id);
}

