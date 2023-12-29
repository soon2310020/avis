package vn.com.twendie.avis.mobile.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.twendie.avis.data.model.Vehicle;

public interface VehicleRepo extends JpaRepository<Vehicle, Long> {
}
