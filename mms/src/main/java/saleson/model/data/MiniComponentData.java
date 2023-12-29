package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Mold;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MiniComponentData {
    private Long id;
    private String code;
    private String name;
    private String type;

    @QueryProjection
    public MiniComponentData(Long id, String name){
        this.id = id;
        this.name = name;
    }
    @QueryProjection
    public MiniComponentData(Long id,String code, String name){
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public MiniComponentData(Mold mold) {
        if(mold != null) {
            this.id = mold.getId();
            this.code = mold.getEquipmentCode();
        }
    }
}
