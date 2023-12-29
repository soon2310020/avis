package com.stg.service.dto.credential;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
public class EmailAndPasswordCredentialDto extends CredentialDto {

    @NotNull
    private String email;
    @NotNull
    private String password;

    @Override
    public <T> T accept(CredentialDtoVisitor<T> v) {
        return v.visit(this);
    }

    @Override
    public String toString() {
        return "EmailAndPasswordCredentialDto{" +
                "Email='" + email + '\'' +
                '}';
    }
}
