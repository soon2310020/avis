package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class CounterToolingData
{
    private Long counterId;
    private String counterCode;
    private String toolingCode;

    @QueryProjection
    public CounterToolingData(Long counterId, String counterCode, String toolingCode)
    {
        this.counterId = counterId;
        this.counterCode = counterCode;
        this.toolingCode = toolingCode;
    }

    @QueryProjection
    public CounterToolingData(Long counterId, String counterCode)
    {
        this.counterId = counterId;
        this.counterCode = counterCode;
    }
}
