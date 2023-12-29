package saleson.model.data.dashboard.maintenance;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MaintenanceMapData {
    private Long total;
    private Long upcoming;
    private Long overdue;
    private List<MaintenanceContinentData> data;
}
