package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.Email;

import java.util.List;

public interface EmailRepo extends JpaRepository<Email, Long> {

    List<Email> findByActiveTrueAndDeletedFalse();

}
