package com.stg.entity.credential;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_and_password_credential_reset_status")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@EqualsAndHashCode(exclude = "emailAndPasswordCredentialReset")
@ToString(exclude = "emailAndPasswordCredentialReset")
public class EmailAndPasswordCredentialResetStatus {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "status_timestamp", nullable = false)
    private LocalDateTime statusTimestamp;

    @ManyToOne
    @JoinColumn(name = "email_and_password_credential_reset_id", nullable = false)
    private EmailAndPasswordCredentialReset emailAndPasswordCredentialReset;

    public EmailAndPasswordCredentialResetStatus(Status status) {
        this.status = status;
    }

    @PrePersist
    public void prePersist() {
        if (getStatusTimestamp() == null) {
            setStatusTimestamp(LocalDateTime.now());
        }
    }

    public enum Status {
        CREATED, VERIFIED, EXPIRED
    }
}
