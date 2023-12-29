package saleson.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.config.Const;
import saleson.common.enumeration.ObjectType;
import saleson.common.util.StringUtils;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@NoArgsConstructor
public class SystemNoteParam {
    private String text;
    private Long objectId;
    @Enumerated(EnumType.STRING)
    private ObjectType objectType;

    @QueryProjection
    public SystemNoteParam(String text, Long objectId) {
        this.text = text;
        this.objectId = objectId;
    }

    public String getMenuKey() {
        if (objectType != null) {
//            String menuKey = Const.menuSystemNoteMap.entrySet().stream().filter(map -> objectType.equals(map.getValue())).map(m -> m.getKey()).findFirst().orElse(null);
            String menuKey = Const.menuObjectTypeMap.entrySet().stream().filter(map -> objectType.equals(map.getValue())).map(m -> m.getKey()).findFirst().orElse(null);
            return menuKey;
        }
        return null;
    }
}
