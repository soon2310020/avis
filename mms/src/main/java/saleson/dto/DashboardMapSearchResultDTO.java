package saleson.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.data.GoogleMapData;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DashboardMapSearchResultDTO {
    private MapToolingDTO toolingObject;
    private List<GoogleMapData.MapData> googleMapData;
    private MapQueryType mapQueryType;

}
