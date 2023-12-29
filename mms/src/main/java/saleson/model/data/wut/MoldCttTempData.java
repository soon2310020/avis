package saleson.model.data.wut;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class MoldCttTempData {
    private Long cdataId;
    private Instant createdAt;
    private Double ct;
    private String lst;
    private String moldCode;
    private Integer shotCount;
    private String ctt;
    private String temp;
    private Long uptimeSeconds;
    private Long moldId;

    @QueryProjection
    public MoldCttTempData(Long cdataId, Instant createdAt, Double ct, String lst, String moldCode, Integer shotCount,
                           String ctt, String temp, Long uptimeSeconds,Long moldId){
        this.cdataId = cdataId;
        this.createdAt = createdAt;
        this.ct = ct;
        this.lst = lst;
        this.moldCode = moldCode;
        this.shotCount = shotCount;
        this.ctt = ctt;
        this.temp = temp;
        this.uptimeSeconds = uptimeSeconds;
        this.moldId =  moldId;
    }
}
