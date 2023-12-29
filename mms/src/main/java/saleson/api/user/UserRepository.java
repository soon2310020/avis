package saleson.api.user;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import saleson.model.Mold;
import saleson.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, QuerydslPredicateExecutor<User>, UserRepositoryCustom {
    Optional<User> findById(Long userId);

    Optional<User> findByEmailAndDeletedIsFalse(String email);

    List<User> findByEmailInAndDeletedIsFalse(List<String> emails);

    @Query("SELECT u FROM User u WHERE (u.loginId = :loginId OR u.email = :email) AND u.deleted = false ")
    Optional<User> findByLoginIdOrEmailAndDeletedIsFalse(@Param("loginId") String loginId,@Param("email")  String email);

    List<User> findByIdInAndDeletedIsFalse(List<Long> userIds);

    Optional<User> findByLoginIdAndDeletedIsFalse(String loginId);

    Boolean existsByLoginIdAndDeletedIsFalse(String loginId);

    Boolean existsByEmailAndDeletedIsFalse(String email);

    List<User> findByAdminIsTrueAndAccessRequestIsTrueAndDeletedIsFalse();

    List<User> findByAdminIsTrueAndDeletedIsFalse();

    @Query(value = "SELECT USER.* FROM USER" +
            " INNER JOIN COMPANY ON USER.COMPANY_ID = COMPANY.ID" +
            " WHERE COMPANY_TYPE = :companyType AND DELETED = 'N'" +
            " ORDER BY USER.NAME ASC",
        nativeQuery = true
    )
    List<User> findByCompanyTypeAndOrderByName(@Param("companyType") String companyType);

    Long countByRequestedAndDeletedIsFalse(Boolean requested);

    List<User> findByCompanyIdAndDeletedIsFalse(Long companyId);

    List<User> findByCompanyIdInAndDeletedIsFalse(List<Long> companyIds);

	@Query(
			value = "SELECT COUNT(*) FROM USER_ROLE WHERE ROLE_ID = :roleId",
			nativeQuery = true
	)
    Long countByRoleId(@Param("roleId") Long roleId);

	Optional<User> findBySsoIdAndDeletedIsFalse(String username);

	List<User> findAllByEnabledIsTrueAndDeletedIsFalse();

    @Query("UPDATE User u SET u.failedAttempt = ?1 WHERE u.loginId = ?2")
    @Modifying
    public void updateFailedAttempts(int failAttempts, String email);

    List<User> findByEnabledIsTrueAndDeletedIsFalseOrderByName();

    List<User> findAllByOrderByIdDesc();
    List<User> findByIdInOrderByIdDesc(List<Long> ids);
    List<User> findByLastLoginAndDeletedIsFalse(Instant moment);

}
