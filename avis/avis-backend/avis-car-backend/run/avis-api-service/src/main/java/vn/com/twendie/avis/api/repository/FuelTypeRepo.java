package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.FuelType;

import java.util.List;
import java.util.Optional;

public interface FuelTypeRepo extends JpaRepository<FuelType, Long> {

    Optional<FuelType> findByCode(String code);

    List<FuelType> findAllByDeletedFalseOrderById();

    List<FuelType> findByFuelTypeGroupIdAndDeletedFalse(Long id);

}
