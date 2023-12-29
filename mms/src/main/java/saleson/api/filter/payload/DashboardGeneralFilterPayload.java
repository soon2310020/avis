package saleson.api.filter.payload;

import lombok.Data;
import saleson.model.Company;
import saleson.model.Location;
import saleson.model.Part;

import java.util.List;

@Data
public class DashboardGeneralFilterPayload {
    private boolean allPart;
    private boolean allSupplier;
    private boolean allToolMaker;
    private boolean allLocation;
    private List<Long> partIds;
    private List<Long> supplierIds;
    private List<Long> toolMakerIds;
    private List<Long> locationIds;
}
