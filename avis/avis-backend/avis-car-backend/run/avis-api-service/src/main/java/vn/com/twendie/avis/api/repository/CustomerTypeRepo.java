package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.CustomerType;

import java.util.Optional;

public interface CustomerTypeRepo extends JpaRepository<CustomerType, Long> {

    Optional<CustomerType> findByIdAndDeletedFalse(Long id);
}
