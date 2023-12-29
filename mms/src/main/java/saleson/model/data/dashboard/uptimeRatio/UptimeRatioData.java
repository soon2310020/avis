package saleson.model.data.dashboard.uptimeRatio;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
public class UptimeRatioData {
    private Integer neverCount;
    private Integer rarelyCount;
    private Integer occasionallyCount;
    private Integer frequentlyCount;
    private Page<UptimeRatioDetails> details;
}
