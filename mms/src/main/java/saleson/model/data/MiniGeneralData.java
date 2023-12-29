package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MiniGeneralData {
    private Long refId;
    private Long objId;
    private String name;
    private String code;

    @QueryProjection
    public MiniGeneralData(Long refId, Long objId, String name, String code){
        this.refId = refId;
        this.objId = objId;
        this.name = name;
        this.code = code;
    }
}
