package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

@Data
public class PartShotData {
    private String title;
    private Long partId;
    private String partCode;
    private Integer shot;
    private Integer partProduced;

    @QueryProjection
    public PartShotData(String title, Long partId, String partCode, Integer shot, Integer partProduced) {
        this.title = title;
        this.partId = partId;
        this.partCode = partCode;
        this.shot = shot;
        this.partProduced = partProduced;
    }
}
