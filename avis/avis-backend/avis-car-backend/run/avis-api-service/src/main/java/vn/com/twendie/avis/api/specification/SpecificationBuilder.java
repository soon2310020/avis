package vn.com.twendie.avis.api.specification;

import org.springframework.data.jpa.domain.Specification;

public interface SpecificationBuilder<T> {

    Specification<T> build(Object filter);

}
