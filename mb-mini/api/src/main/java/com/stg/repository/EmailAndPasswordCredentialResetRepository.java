package com.stg.repository;

import com.stg.entity.credential.EmailAndPasswordCredentialReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailAndPasswordCredentialResetRepository extends JpaRepository<EmailAndPasswordCredentialReset, Long> {

    EmailAndPasswordCredentialReset findByVerificationTokenAndValidUntilAfter(
            String verificationToken,
            LocalDateTime validUntil);

    @Query("FROM EmailAndPasswordCredentialReset r " +
            "WHERE r.emailAndPasswordCredential.id = ?1 " +
            "AND r.validUntil > ?2 " +
            "AND r.id <> ?3")
    List<EmailAndPasswordCredentialReset> findByEmailAndPasswordCredentialIdAndValidUntilAfterAndIdNot(
            Long emailAndPasswordCredentialId,
            LocalDateTime validUntil,
            Long id);
}
