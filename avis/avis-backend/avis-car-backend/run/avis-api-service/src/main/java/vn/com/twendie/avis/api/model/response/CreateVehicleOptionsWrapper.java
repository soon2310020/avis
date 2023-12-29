package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVehicleOptionsWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("fuel_groups")
    private List<FuelTypeGroupDTO> fuelGroups;

    @JsonProperty("supplier_groups")
    private List<VehicleSupplierGroupDTO> supplierGroups;

    @JsonProperty("branches")
    private List<BranchDTO> branches;

    @JsonProperty("operation_admins")
    private List<OperationAdminDTO> operationAdminDTOS;

    @JsonProperty("unit_operators")
    private List<UnitOperatorDTO> unitOperatorDTOS;

    @JsonProperty("accountants")
    private List<AccountantDTO> accountantDTOS;

}
