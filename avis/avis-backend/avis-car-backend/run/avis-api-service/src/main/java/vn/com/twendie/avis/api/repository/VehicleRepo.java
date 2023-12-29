package vn.com.twendie.avis.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import vn.com.twendie.avis.api.model.projection.VehicleProjection;
import vn.com.twendie.avis.data.model.Vehicle;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VehicleRepo extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {

    @Query(value = "SELECT * FROM vehicle AS v WHERE v.is_deleted = false AND lower(v.number_plate) LIKE lower(concat('%',?1,'%'))", nativeQuery = true)
    List<Vehicle> findAllVehicleByNumberPlate(String numberPlate);

    @Query("SELECT " +
            "   v.id AS id, " +
            "   v.numberPlate AS numberPlate, " +
            "   v.type AS type, " +
            "   v.owner AS owner, " +
            "   v.model AS model, " +
            "   v.operationAdmin.name AS operationAdminName " +
            "FROM Vehicle v " +
            "WHERE v.id IN :ids")
    List<VehicleProjection> findByIdIn(Collection<Long> ids);

    Optional<Vehicle> findByIdAndDeletedFalse(Long id);

    Boolean existsByNumberPlateAndDeletedFalse(String numberPlate);

    Boolean existsByNumberPlateAndIdNotAndDeletedFalse(String numberPlate, Long id);

    List<Vehicle> findByTravelWarrantExpiryDateAndDeletedFalse(Timestamp travelWarrantExpiryDate);

    List<Vehicle> findByRegistrationToDateAndDeletedFalse(Timestamp registrationToDate);

}
