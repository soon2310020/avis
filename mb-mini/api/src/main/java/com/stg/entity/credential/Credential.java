package com.stg.entity.credential;

import com.stg.entity.user.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "credential")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "credential_type", length = 45)
@Data
@EqualsAndHashCode(exclude = "user")
public abstract class Credential {

    private static final boolean DEFAULT_NON_EXPIRED_FLAG = true;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "non_expired")
    private Boolean nonExpired;

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    public abstract <T> T accept(CredentialVisitor<T> v);

    @PrePersist
    public void prePersist() {
        setCreationTime(LocalDateTime.now());

        if (getNonExpired() == null) {
            setNonExpired(DEFAULT_NON_EXPIRED_FLAG);
        }
    }
}
