package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MiniPartData
{
    private Long id;
    private String partCode;
    private String name;

    @QueryProjection
    public MiniPartData(Long id, String partCode)
    {
        this.id = id;
        this.partCode = partCode;
    }
}
