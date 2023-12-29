package vn.com.twendie.avis.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.twendie.avis.data.enumtype.UserRoleEnum;
import vn.com.twendie.avis.data.model.MemberCustomer;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface MemberCustomerRepo extends JpaRepository<MemberCustomer, Long>, JpaSpecificationExecutor<MemberCustomer> {

    @Query(value = "SELECT c.id AS customerId, c.name AS customerName, c.tax_code AS taxCode, c.address AS address, c.created_at AS customerCreatedAt, c.id_card AS customerIdCard, c.card_type AS customerCardType, " +

            // -------addition data for excel export-------------------------
            "c.code AS customerCode, c.country_code AS customerCountryCode, c.mobile AS customerMobile, c.email AS customerEmail, c.bank_account_number AS bankAccountNumber, c.bank_account_holder AS bankAccountHolder, c.bank_name AS bankName, ct.name AS customerTypeName, " +
            "mc.code AS memberCode, mc.role AS memberRole, mc.department AS memberDepartment, mc.mobile AS memberMobile, mc.country_code AS memberCountryCode, mc.email AS memberEmail, " +
            // --------------------------------------------------------------

            "mc.id AS memberCustomerId, mc.name AS adminName, mc.created_at AS memberCustomerCreatedAt, ct.code AS customerType, u.name AS createdBy " +
            "FROM customer c " +
            "LEFT JOIN member_customer mc ON mc.customer_id = c.id AND mc.role = 'Admin' AND mc.is_deleted = false " +
            "LEFT JOIN user u ON c.created_by_id = u.id AND u.is_deleted = false " +
            "LEFT JOIN customer_type ct on c.customer_type_id = ct.id " +
            "WHERE lower(c.name) LIKE lower(:customerName) " +
            "AND (:customerTaxCode IS NULL OR lower(c.tax_code) LIKE lower(:customerTaxCode)) " +
            "AND lower(c.address) LIKE lower(:customerAddress) " +
            "AND (:adminName IS NULL OR lower(mc.name) LIKE lower(:adminName)) " +
            "AND (:createdBy IS NULL OR lower(u.name) LIKE lower(:createdBy)) " +
            "AND (:createdFrom IS NULL OR (mc.id IS NOT NULL AND mc.created_at >= :createdFrom) OR (mc.id IS NULL AND c.created_at >= :createdFrom)) " +
            "AND (:createdTo IS NULL OR (mc.id IS NOT NULL AND mc.created_at <= :createdTo) OR (mc.id IS NULL AND c.created_at <= :createdTo)) " +
            "AND c.is_deleted = false " +
            "ORDER BY c.name , mc.created_at DESC, c.created_at DESC",
            countProjection = "c.id",
            nativeQuery = true)
    Page<AdminCustomerProjection> filterAdminCustomers(String customerName, String customerTaxCode, String customerAddress, String adminName, String createdBy, Timestamp createdFrom, Timestamp createdTo, Pageable pageable);

    Boolean existsByMobileAndCountryCodeAndDeletedFalse(String phoneNumber, String countryCode);

    Boolean existsByMobileAndCountryCodeAndIdNotAndDeletedFalse(String phoneNumber, String countryCode, Long id);

    @Query(nativeQuery = true, value = "select count(*) from member_customer m  " +
            "where m.mobile = :mobile and m.country_code = :countryCode and m.id != :memberId and m.is_deleted = false and m.parent_id is not null")
    Long countByMobileAndCountryCodeAndIdNotAndDeletedFalseAndParentIdNotNull(@Param("mobile") String mobile,
                                                                          @Param("countryCode") String countryCode,
                                                                          @Param("memberId") Long memberId);

    @Query(nativeQuery = true, value = "select count(*) from member_customer m  " +
            "where m.email = :email and m.id != :memberId and m.is_deleted = false and m.parent_id is not null")
    Long countByEmailAndIdNotAndDeletedFalse(@Param("email") String email,
                                                           @Param("memberId") Long memberId);

    Optional<MemberCustomer> findByIdAndDeletedFalse(Long id);

    Optional<MemberCustomer> findByIdAndCustomerIdAndDeletedFalse(Long memberId, Long customerId);

    Boolean existsByCustomerIdAndDeletedFalse(Long customerId);

    @Query(value = "SELECT MAX(code) FROM member_customer WHERE code LIKE CONCAT(?1, '%')", nativeQuery = true)
    String getNewestCodeByPrefix(String prefix);

    Boolean existsByCodeAndIdNot(String code, Long id);

    Boolean existsByCustomerIdAndDeletedFalseAndRoleAndIdNotAndActiveTrue(Long customerId, String role, Long adminId);

    List<MemberCustomer> findByParentId(Long id);

    List<MemberCustomer> findByParentIdAndDeletedFalse(Long id);

    List<MemberCustomer> findByUserIsNull();

    @Query(value = "select * from member_customer m where m.role=:role and m.parent_id=:parentId and lower(m.name) like concat(lower(:name), '%')", nativeQuery = true)
    List<MemberCustomer> findByParentIdAndIsDeletedIsFalseAndRoleIsAndNameLike(String role, Long parentId, String name);

    @Query(nativeQuery = true, value = "select * from member_customer m where lower(m.name) like concat('%',lower(:name), '%') and m.role != :role")
    List<MemberCustomer> getMemberCustomerByName(@Param("name") String name,
                                                 @Param("role") String role);

//    @Query(value = "SELECT DISTINCT m.* from member_customer m inner JOIN contract c on c.member_customer_id = m.id or m.parent_id = c.member_customer_id WHERE c.id = :contractId " +
//            "and lower(m.name) like concat('%',lower(:name), '%') and m.role != :role and m.active = true and m.is_deleted = false", nativeQuery = true)
    @Query(value = "SELECT DISTINCT m.* from member_customer m inner JOIN contract c on m.parent_id = c.member_customer_id WHERE c.id = :contractId " +
        "and lower(m.name) like concat('%',lower(:name), '%') and m.role != :role and m.is_deleted = false", nativeQuery = true)
    List<MemberCustomer> getMemberCustomerByNameAndContractId(@Param("name") String name,
                                                 @Param("role") String role,
                                                 @Param("contractId") Long contractId);

    @Query(value = "select m.name from member_customer m where m.id in :ids", nativeQuery = true)
    List<String> findNameByIdIn(@Param("ids") List<Long> ids);
}
