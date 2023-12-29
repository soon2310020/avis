package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MoldShotData {
    private String title;
    private Long moldId;
    private String moldCode;
    private Integer shotCount;
    private Double avgCavity;
    private Integer quantity;

    @QueryProjection
    public MoldShotData(String title, String moldCode, Integer shotCount, Integer quantity){
        this.title = title;
        this.moldCode = moldCode;
        this.shotCount = shotCount;
        this.quantity = quantity;
    }

    @QueryProjection
    public MoldShotData(String title, Long moldId, Integer shotCount, Integer quantity){
        this.title = title;
        this.moldId = moldId;
        this.shotCount = shotCount;
        this.quantity = quantity;
    }

    @QueryProjection
    public MoldShotData(String title, Long moldId, Integer quantity){
        this.title = title;
        this.moldId = moldId;
        this.quantity = quantity;
    }
}
