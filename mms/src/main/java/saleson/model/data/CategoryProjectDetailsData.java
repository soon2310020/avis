package saleson.model.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryProjectDetailsData {
    private Long projectId;
    private String projectName;
    private Integer partCount;
    private Integer moldCount;
    List<CategoryPartDetailsData> partDetailsData;
}
