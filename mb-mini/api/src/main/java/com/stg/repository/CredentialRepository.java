package com.stg.repository;

import com.stg.entity.credential.Credential;
import com.stg.entity.credential.EmailAndPasswordCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {

    @Query("SELECT upc FROM EmailAndPasswordCredential upc JOIN upc.user u " +
            "WHERE u.enabled = true " +
            "AND upc.email = ?1")
    Optional<EmailAndPasswordCredential> findByEmailAndEnabledTrue(String email);

    @Query("SELECT upc FROM EmailAndPasswordCredential upc JOIN upc.user u " +
            "WHERE u.enabled = false " +
            "AND upc.email = ?1")
    Optional<EmailAndPasswordCredential> findByEmailAndEnabledFalse(String email);

    @Query("SELECT upc FROM EmailAndPasswordCredential upc JOIN upc.user u " +
            "WHERE u.enabled = true " +
            "AND upc.email in ?1")
    List<EmailAndPasswordCredential> findByEmailAndEnabledTrue(List<String> email);

}
