package saleson.model.clone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CounterVersionDto {
    private String counterId;
    private Long toolingId;
    private String toolingName;
    private Integer presetCount;
    private String installedAt;
    private String installedBy;
    private String photo;
    private List<String> photos;
    private String memo;
}
