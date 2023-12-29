package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.RentalServiceType;

import java.util.List;
import java.util.Optional;

public interface RentalServiceTypeRepo extends JpaRepository<RentalServiceType, Long> {

    Optional<RentalServiceType> findByCode(String code);

    List<RentalServiceType> findAllByDeletedFalseAndContractTypeIdOrderById(Long id);

}
