package saleson.model.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TotalToolingData {
    private List<GoogleMapData.MapData> mapChartDataList = Lists.newArrayList();
    private List<CountLocationMold> countLocationMoldList = Lists.newArrayList();
}
