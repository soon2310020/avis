package saleson.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.util.DataUtils;
import saleson.model.Category;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    private Long id;
    @JsonProperty("parent_id")
    private Long parentId;
    private String name;
    private String description;
    private Integer level;
    @JsonProperty("sort_order")
    private Integer sortOrder;
    private boolean enabled;

    public static CategoryDTO convertToDTO(Category category){
        CategoryDTO categoryDTO = DataUtils.mapper.map(category,CategoryDTO.class);
        return categoryDTO;
    }

}
