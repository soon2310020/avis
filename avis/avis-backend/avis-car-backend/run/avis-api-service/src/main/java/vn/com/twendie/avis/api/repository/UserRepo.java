package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.twendie.avis.api.model.projection.UserProjection;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.data.model.UserRole;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @Query(value = "SELECT * FROM user AS u WHERE u.is_deleted = false" +
            " AND lower(u.name) LIKE lower(concat('%',?1,'%'))" +
            " AND u.user_role_id = ?2" +
            " AND u.user_group_id in ?3" +
            " AND u.active = true",
            nativeQuery = true)
    List<User> findAllAvisDriverByName(String driverName, Long roleId, List<Long> groupIds);

    @Query(value = "SELECT * FROM user AS u WHERE u.is_deleted = false" +
            " AND lower(u.name) LIKE lower(concat('%',?1,'%'))" +
            " AND u.user_role_id = ?2" +
            " AND u.user_group_id = ?3" +
            " AND u.active = true",
            nativeQuery = true)
    List<User> findAllCustomerDriverByName(String driverName, Long roleId, Long groupId);

    @Query("SELECT " +
            "   u.id AS id, " +
            "   u.name AS name " +
            "FROM User u " +
            "WHERE u.id IN :ids")
    List<UserProjection> findByIdIn(Collection<Long> ids);

    Optional<User> findByIdAndUserRoleNameAndUserGroupIdInAndDeletedFalse(Long id, String userRoleName, Collection<Long> userGroup_id);

    Boolean existsByUsernameAndIdNot(String username, Long id);

    Boolean existsByUsername(String username);

    Boolean existsByCodeAndIdNot(String code, Long id);

    Boolean existsByIdCardAndCardTypeAndUserGroupIdAndIdNotAndUserRoleIdAndDeletedFalse(String idCard, Integer cardType, Long groupId, Long driverId, Long roleId);

    @Query(value = "SELECT MAX(code) FROM user WHERE code LIKE CONCAT(?1, '%')", nativeQuery = true)
    String getNewestCodeByPrefix(String prefix);

    Boolean existsByMobileAndCountryCodeAndUserGroupIdAndIdNotAndDeletedFalse(String mobile, String countryCode, Long userGroupId, Long driverId);

    Optional<User> findByIdAndDeletedFalse(Long id);

    Optional<User> findByIdAndDepartmentIdAndActiveTrueAndDeletedFalse(Long id, Long userRoleId);

    List<User> findByDriverLicenseExpiryDateAndDeletedFalse(Timestamp driverLicenseExpiryDate);

    @Query(value = "select u.email from user u where u.email = :email limit 1", nativeQuery = true)
    String findFirstEmailByEmail(@Param("email") String email);

    User findByEmailAndActiveIsTrueAndUserRoleNot(String email, UserRole userRole);

    User findByEmailOrMobileFullAndActiveIsTrueAndUserRoleNot(String email, String mobilePhone, UserRole userRole);

    String findMobileFullByUsername(String username);

    User findByUsername(String username);

}
