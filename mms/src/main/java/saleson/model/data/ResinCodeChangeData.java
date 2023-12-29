package saleson.model.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.ResinCodeChange;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResinCodeChangeData {
    private String oldResinCode;
    private String newResinCode;
    private Double oldWACT;
    private Double newWACT;
    private Instant time;

    public ResinCodeChangeData(ResinCodeChange resinCodeChange) {
        this.oldResinCode = resinCodeChange.getOldResinCode();
        this.newResinCode = resinCodeChange.getNewResinCode();
        this.oldWACT = resinCodeChange.getCurrentWact();
        this.time = resinCodeChange.getTime();
    }
}
