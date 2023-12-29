package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.twendie.avis.data.enumtype.VehicleStatusEnum;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleFilterOptionsWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("supplier_groups")
    private List<VehicleSupplierGroupDTO> supplierGroupDTOS;

    @JsonProperty("status")
    private List<VehicleStatusEnum> vehicleStatusEnums;

}
