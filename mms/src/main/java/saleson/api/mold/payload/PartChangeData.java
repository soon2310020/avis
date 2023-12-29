package saleson.api.mold.payload;


import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class PartChangeData {
    private List<PartLiteData> original;
    private List<PartLiteData> changed;

    private Instant dateTime;
    private String reportedBy;
}
