package saleson.api.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.Category;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long>, QuerydslPredicateExecutor<Category>, CategoryRepositoryCustom {

	Optional<Category> findById(Long id);

	@Query("SELECT c FROM Category c WHERE c.level = 3 AND c.grandParentId =:grandParentId AND c.enabled =:enable")
	List<Category> findByGrandParentId(Long grandParentId, boolean enable);

	List<Category> findAllByLevel(Integer level);
	List<Category> findAllByLevelAndEnabledIsTrue(Integer level);

	Boolean existsCategoryByName(String name);
	Boolean existsCategoryByNameAndIdNot(String name,Long id);

	List<Category> findByCreatedAtAfterAndCreatedByIn(Instant date, List<Long> userIds);

	Boolean existsCategoryByNameAndLevel(String name, Integer level);

	Boolean existsCategoryByNameAndLevelAndIdNot(String name, Integer level, Long id);

	Boolean existsCategoryByNameAndLevelAndParentId(String name, Integer level, Long parentId);

	Boolean existsCategoryByNameAndLevelAndParentIdAndIdNot(String name, Integer level, Long parentId, Long id);

	Optional<Category> findFirstByName(String name);

}
