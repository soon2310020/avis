package saleson.api.menu;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import saleson.model.Menu;

@Deprecated
@Repository
public interface MenuRepository extends JpaRepository<Menu, Long>, QuerydslPredicateExecutor<Menu> {
	@EntityGraph(attributePaths = { "parent", "children" })
	List<Menu> findMenusByParentId(Long parentId);
}
