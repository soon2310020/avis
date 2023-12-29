package saleson.api.role;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import saleson.common.enumeration.GraphType;
import saleson.model.RoleGraph;

@Deprecated
@Repository
public interface RoleGraphRepository extends JpaRepository<RoleGraph, Long>, QuerydslPredicateExecutor<RoleGraph> {
	List<RoleGraph> findByRoleId(Long roleId);

	List<RoleGraph> findByRoleIdIn(List<Long> roleIds);

	@Query("SELECT DISTINCT graphType FROM RoleGraph WHERE roleId IN :roleIds")
	List<GraphType> findGraphTypeByRoleIdIn(@Param("roleIds") List<Long> roleIds);

	@Query(nativeQuery = true, value = "Select * from ROLE_GRAPH where ID in ?1")
	List<RoleGraph> findByIdIn(List<Long> roleGraphIds);
}
