package saleson.api.role;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import saleson.common.enumeration.RoleType;
import saleson.model.Role;

@Deprecated
@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, QuerydslPredicateExecutor<Role> {
	Optional<Role> findByAuthority(String role_admin);

	List<Role> findRolesByRoleType(RoleType roleType);

	List<Role> findByIdIsInAndRoleType(List<Long> ids, RoleType roleType);

	List<Role> findAllByRoleType(RoleType roleType);

	Boolean existsRoleByRoleTypeAndAuthority(RoleType roleType, String authority);

	Boolean existsRoleByRoleTypeAndName(RoleType roleType, String name);

	Boolean existsRoleByRoleTypeAndAuthorityAndIdNot(RoleType roleType, String authority, Long id);

	Boolean existsRoleByRoleTypeAndNameAndIdNot(RoleType roleType, String name, Long id);

}
