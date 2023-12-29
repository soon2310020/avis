package saleson.model.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDetailsData {
    private Long categoryId;
    private String categoryName;
    private Integer projectCount;
    private Integer partCount;
    private Integer moldCount;
    private List<CategoryProjectDetailsData> projectDetailsData;
}
