package com.stg.entity.user;

import com.stg.entity.credential.Credential;
import com.stg.entity.credential.EmailAndPasswordCredential;
import com.stg.errors.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class User {

    public enum Role {ADMIN, SUPER_ADMIN}

    private static final boolean DEFAULT_NON_LOCKED_FLAG = true;
    private static final boolean DEFAULT_ENABLED_FLAG = true;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "non_locked")
    private Boolean nonLocked;  // lock account nếu đăng nhập fail nhiều lần

    @Column(name = "enabled")
    private Boolean enabled;    // user không hoạt động

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL)
    @Fetch(FetchMode.SELECT)
    private Set<Credential> credentials = new HashSet<>();

    @LastModifiedDate
    @Column(name = "last_updated_timestamp")
    private LocalDateTime lastUpdatedTimestamp;

    @Column(name = "full_name")
    private String fullName;

    public <T> T accept(UserVisitor<T> v) {
        return v.visit(this);
    }

    private void validateCredentials(Set<Credential> credentials) {
        Map<? extends Class<? extends Credential>, List<Credential>> credentialsByType = credentials.stream()
                .collect(Collectors.groupingBy(Credential::getClass));

        if (credentialsByType.get(EmailAndPasswordCredential.class).isEmpty()) {
            throw new ValidationException("An user must have one email and password credential");
        } else if (credentialsByType.get(EmailAndPasswordCredential.class).size() > 1) {
            throw new ValidationException("An user can only have one email and password credential");
        }
    }

    public void addCredential(Credential credential) {
        if (getCredentials() == null) {
            setCredentials(new HashSet<>());
        }

        Set<Credential> allCredentials = new HashSet<>(getCredentials());

        if (allCredentials.add(credential)) {
            validateCredentials(allCredentials);

            credential.setUser(this);
            getCredentials().add(credential);
        }
    }

    @PrePersist
    public void prePersist() {
        if (creationTime == null) {
            creationTime = LocalDateTime.now();
        }
        if (lastUpdatedTimestamp == null) {
            lastUpdatedTimestamp = LocalDateTime.now();
        }
        if (getEnabled() == null) {
            setEnabled(DEFAULT_ENABLED_FLAG);
        }
        if (getNonLocked() == null) {
            setNonLocked(DEFAULT_NON_LOCKED_FLAG);
        }

        if (getCredentials() != null) {
            validateCredentials(getCredentials());
            credentials.forEach(c -> c.setUser(this));
        }
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdatedTimestamp = LocalDateTime.now();
    }

}
