package saleson.api.mold;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.MoldAuthority;


public interface MoldAuthorityRepository extends JpaRepository<MoldAuthority, Long>, QuerydslPredicateExecutor<MoldAuthority> {
	void deleteAllByMoldId(Long moldId);

	boolean existsByAuthority(String authority);

}
