package saleson.api.imageupload.dto;

import com.emoldino.framework.util.ValueUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Category;

@Data
@NoArgsConstructor
public class BrandDto {
    private Long id;
    private String name;
    private Long numImages;
    private String categoryName;

    public BrandDto(Category model, Long numImages) {
        ValueUtils.map(model, this);
        if (model.getParent() != null) {
            this.categoryName = model.getParent().getName();
        }
        this.numImages = numImages;
    }
}
