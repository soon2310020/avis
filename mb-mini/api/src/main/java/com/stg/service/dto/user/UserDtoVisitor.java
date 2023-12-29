package com.stg.service.dto.user;

public interface UserDtoVisitor<T> {

    T visit(UserDto dto);

}
