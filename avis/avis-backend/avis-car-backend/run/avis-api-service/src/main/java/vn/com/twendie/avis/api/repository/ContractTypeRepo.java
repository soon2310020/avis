package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.ContractType;

import java.util.List;

public interface ContractTypeRepo extends JpaRepository<ContractType, Long> {

    List<ContractType> findAllByDeletedFalse();
}