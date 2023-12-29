package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.VehicleSupplierGroup;

import java.util.List;
import java.util.Optional;

public interface VehicleSupplierGroupRepo extends JpaRepository<VehicleSupplierGroup, Long> {

    Optional<VehicleSupplierGroup> findByIdAndDeletedFalse(Long id);

    List<VehicleSupplierGroup> findByDeletedFalse();
}
