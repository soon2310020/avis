package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;
import saleson.model.Terminal;

import java.time.Instant;

@Data
@Builder
public class TerminalData implements Comparable<TerminalData>{
    private Terminal terminal;
    private Long numberOfCounter;

    @QueryProjection
    @Builder
    public TerminalData(Terminal terminal, Long numberOfCounter){
        this.terminal = terminal;
        this.numberOfCounter = numberOfCounter;
    }

    @Override
    public int compareTo(TerminalData terminalData) {
        if(this.numberOfCounter > terminalData.getNumberOfCounter()) return 1;
        else if(this.numberOfCounter < terminalData.getNumberOfCounter()) return -1;
        return 0;
    }
}
