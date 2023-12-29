package saleson.model.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.ContinentName;

import java.util.ArrayList;
import java.util.List;

@Data
public class ContinentStatisticData {
    private String continent;
    private List<LocationMoldData> locationMoldData;
    private Double percentage;

    public ContinentStatisticData(ContinentName continent){
        this.continent = continent.getTitle();
        this.locationMoldData = new ArrayList<>();
        this.percentage = 0.0;
    }
}
