package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.FuelTypeGroup;

import java.util.List;
import java.util.Optional;

public interface FuelTypeGroupRepo extends JpaRepository<FuelTypeGroup, Long> {

    Optional<FuelTypeGroup> findByIdAndDeletedFalse(Long id);

    List<FuelTypeGroup> findByDeletedFalse();
}
