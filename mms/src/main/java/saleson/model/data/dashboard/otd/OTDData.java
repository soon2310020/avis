package saleson.model.data.dashboard.otd;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OTDData {
    private Integer demand;
    private Integer output;
    private Double overall;
    private List<OTDDetailsData> details;
    private Integer totalPages;
    private Integer number;
}
