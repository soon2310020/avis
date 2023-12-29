package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;

@Data
public class TerminalLocationCode {
    String terminalCode;
    String locationCode;
    String installationArea;

    @Builder
    public TerminalLocationCode(String terminalCode, String locationCode, String installationArea){
        this.terminalCode = terminalCode;
        this.locationCode = locationCode;
        this.installationArea = installationArea;
    }
}
