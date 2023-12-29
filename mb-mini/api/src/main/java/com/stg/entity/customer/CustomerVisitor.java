package com.stg.entity.customer;

public interface CustomerVisitor<T> {
    T visit(Customer customer);
}
