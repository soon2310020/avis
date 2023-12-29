package com.stg.entity.credential;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "email_and_password_credential", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@DiscriminatorValue(value = EmailAndPasswordCredential.CREDENTIAL_TYPE)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class EmailAndPasswordCredential extends Credential {

    public static final String CREDENTIAL_TYPE = "email_and_password";

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Override
    public <T> T accept(CredentialVisitor<T> v) {
        return v.visit(this);
    }

    @Override
    public String toString() {
        return "EmailAndPasswordCredential{" +
                "email='" + email + '\'' +
                "} " + super.toString();
    }
}
