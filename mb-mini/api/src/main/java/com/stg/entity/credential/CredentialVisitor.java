package com.stg.entity.credential;

public interface CredentialVisitor<T> {

    T visit(EmailAndPasswordCredential entity);

}
