package saleson.model.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class MaintenanceLog {
    private List<MaintenanceHistoryData> histories;
    private Double executionRate;
    private Long moldId;
    private String equipmentCode;
    private String toolMaker;
    private String engineer;

    public MaintenanceLog(){
        histories = new ArrayList<>();
        executionRate = 0.0;
        equipmentCode = "";
        toolMaker = "";
    }
}
