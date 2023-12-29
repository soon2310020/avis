package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.CostType;

import java.util.Optional;

public interface CostTypeRepo extends JpaRepository<CostType, Long> {

    Optional<CostType> findByCode(String code);

}
