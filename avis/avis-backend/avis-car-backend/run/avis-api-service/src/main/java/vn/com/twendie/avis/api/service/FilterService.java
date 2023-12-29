package vn.com.twendie.avis.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.com.twendie.avis.api.model.filter.FilterWrapper;
import vn.com.twendie.avis.api.specification.SpecificationBuilder;

import java.io.Serializable;

public interface FilterService<T, I extends Serializable> {

    Page<T> filter(JpaSpecificationExecutor<T> executor,
                   SpecificationBuilder<T> specificationBuilder,
                   FilterWrapper<?> filterWrapper);

}
