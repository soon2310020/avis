package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.api.model.payload.VehiclePayload;
import vn.com.twendie.avis.api.model.payload.VehicleSuggestionPayload;
import vn.com.twendie.avis.api.model.projection.VehicleProjection;
import vn.com.twendie.avis.api.model.response.CreateVehicleOptionsWrapper;
import vn.com.twendie.avis.api.model.response.VehicleFilterOptionsWrapper;
import vn.com.twendie.avis.api.model.response.VehicleDTO;
import vn.com.twendie.avis.api.model.response.VehicleDetailDTO;
import vn.com.twendie.avis.api.rest.model.GeneralPageResponse;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.Vehicle;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;

public interface VehicleService {

    Vehicle findById(Long id);

    Map<Long, VehicleProjection> findByIdIn(Collection<Long> ids);

    Vehicle save(Vehicle vehicle);

    GeneralPageResponse<VehicleDTO> vehicleSuggestionsByNumberPlate(VehicleSuggestionPayload payload);

    Vehicle findByIdIgnoreDelete(Long id);

    Boolean deleteVehicle(Long id);

    CreateVehicleOptionsWrapper getCreateOptions();

    VehicleDetailDTO createVehicle(VehiclePayload payload);

    VehicleDetailDTO updateVehicle(VehiclePayload payload);

    void assignToContract(Vehicle vehicle, Contract contract);

    void unAssignFromContract(Vehicle vehicle, Contract contract);

    void updateVehicleStatus(Vehicle vehicle);

    VehicleFilterOptionsWrapper getFilterOptions();

    Vehicle getContractVehicleAtTime(Contract contract, Timestamp timestamp);
}
