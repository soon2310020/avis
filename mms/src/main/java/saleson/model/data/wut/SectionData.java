package saleson.model.data.wut;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class SectionData {
    private Double hour;
    private Integer shotCount;
    private String startedAt;
}
