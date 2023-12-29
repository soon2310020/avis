package saleson.api.workOrder.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.model.Location;
import saleson.model.WorkOrderAsset;
import saleson.model.checklist.ChecklistType;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class WorkOrderDraft {
    WorkOrderPayload payload;
    List<WorkOrderAsset> workOrderAssets = new ArrayList<>();
    private List<Location> plantList = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private ChecklistType checklistType;

    public WorkOrderDraft(WorkOrderPayload payload, List<WorkOrderAsset> workOrderAssets, List<Location> plantList) {
        this.payload = payload;
        this.workOrderAssets = workOrderAssets;
        this.plantList = plantList;
    }
}
