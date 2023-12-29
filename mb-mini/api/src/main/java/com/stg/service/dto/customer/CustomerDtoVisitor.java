package com.stg.service.dto.customer;


public interface CustomerDtoVisitor<T> {
    T visit(CustomerDetailDto dto);
}
