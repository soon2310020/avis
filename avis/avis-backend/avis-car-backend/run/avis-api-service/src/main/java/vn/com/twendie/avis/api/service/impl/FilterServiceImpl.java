package vn.com.twendie.avis.api.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.core.util.StrUtils;
import vn.com.twendie.avis.api.model.filter.FilterWrapper;
import vn.com.twendie.avis.api.service.FilterService;
import vn.com.twendie.avis.api.specification.SpecificationBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.*;

@Service
public class FilterServiceImpl<T, I extends Serializable> implements FilterService<T, I> {

    private final StrUtils strUtils;

    public FilterServiceImpl(StrUtils strUtils) {
        this.strUtils = strUtils;
    }

    @Override
    public Page<T> filter(JpaSpecificationExecutor<T> executor,
                          SpecificationBuilder<T> specificationBuilder,
                          FilterWrapper<?> filterWrapper) {
        List<Order> orders = filterWrapper.getSortBy().stream()
                .map(order -> {
                    Direction direction = order.startsWith("-") ? DESC : ASC;
                    String property = strUtils.toCamelCase(order.replace("-", ""));
                    return Order.by(property).with(direction);
                })
                .collect(Collectors.toList());
        return executor.findAll(specificationBuilder.build(filterWrapper.getFilter()),
                PageRequest.of(filterWrapper.getPage() - 1, filterWrapper.getSize(), Sort.by(orders)));
    }

}
