package com.stg.entity.credential;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "email_and_password_credential_reset")
@Data
@Accessors(chain = true)
public class EmailAndPasswordCredentialReset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "email_and_password_credential_id", nullable = false)
    private EmailAndPasswordCredential emailAndPasswordCredential;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;

    @Column(name = "verification_token", nullable = false)
    private String verificationToken;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "emailAndPasswordCredentialReset",
            fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    private List<EmailAndPasswordCredentialResetStatus> statuses;

    public List<EmailAndPasswordCredentialResetStatus> getStatuses() {
        if (statuses == null) {
            statuses = new ArrayList<>();
        }

        return statuses;
    }

    public boolean isExpired() {
        return validUntil != null && validUntil.isBefore(LocalDateTime.now());
    }

    public EmailAndPasswordCredentialResetStatus getLastStatus() {
        return getStatuses().stream()
                .sorted(Comparator.comparing(EmailAndPasswordCredentialResetStatus::getStatusTimestamp).reversed())
                .findFirst()
                .orElse(null);
    }

    public void addStatus(EmailAndPasswordCredentialResetStatus status) {
        status.setEmailAndPasswordCredentialReset(this);
        getStatuses().add(status);
    }

    @PrePersist
    public void prePersist() {
        if (getValidFrom() == null) {
            setValidFrom(LocalDateTime.now());
        }

        if (getValidUntil() == null) {
            setValidUntil(getValidFrom().plusWeeks(1));
        }

        if (getVerificationToken() == null) {
            setVerificationToken(DigestUtils.sha256Hex(UUID.randomUUID().toString()));
        }
    }
}
