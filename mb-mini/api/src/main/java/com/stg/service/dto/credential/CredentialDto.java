package com.stg.service.dto.credential;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        defaultImpl = EmailAndPasswordCredentialDto.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EmailAndPasswordCredentialDto.class, name = "emailAndPassword")

})
@Data
@Accessors(chain = true)
public abstract class CredentialDto {

    private Boolean nonExpired;
    private LocalDateTime creationTime;

    public abstract <T> T accept(CredentialDtoVisitor<T> v);
}
