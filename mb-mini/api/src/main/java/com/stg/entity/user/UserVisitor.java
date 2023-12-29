package com.stg.entity.user;

public interface UserVisitor<T> {

    T visit(User entity);

}
