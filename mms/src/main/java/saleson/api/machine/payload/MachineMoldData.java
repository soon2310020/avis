package saleson.api.machine.payload;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MachineMoldData {
    private Long id;
    private String name;
    private Long matchedId;
    private String matchedName;

    @QueryProjection
    public MachineMoldData(Long id, String name, Long matchedId, String matchedName) {
        this.id = id;
        this.name = name;
        this.matchedId = matchedId;
        this.matchedName = matchedName;
    }
}
