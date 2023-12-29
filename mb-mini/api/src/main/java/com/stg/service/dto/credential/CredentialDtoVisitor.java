package com.stg.service.dto.credential;

public interface CredentialDtoVisitor<T> {

    T visit(EmailAndPasswordCredentialDto dto);

}
