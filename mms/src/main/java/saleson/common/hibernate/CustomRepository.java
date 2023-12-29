package saleson.common.hibernate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomRepository<T, P> {

	//Optional<T> findOne(P searchParam);

	Page<T> findAll(P searchParam, Pageable pageable);
}
