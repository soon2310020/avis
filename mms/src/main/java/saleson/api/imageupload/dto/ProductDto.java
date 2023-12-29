package saleson.api.imageupload.dto;

import com.emoldino.framework.util.ValueUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Category;
import saleson.model.Terminal;

@Data
@NoArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private Long numImages;
    private String categoryName;
    private String brandName;

    public ProductDto(Category model, Long numImages) {
        ValueUtils.map(model, this);
        if(model.getGrandParent()!=null){
            this.categoryName = model.getGrandParent().getName();
        }
        Category parent = model.getParent();
        if(model.getLevel()!=null && model.getLevel().equals(3) && parent!=null){
            this.brandName = parent.getName();
            if(parent.getParent()!=null){
                this.categoryName = parent.getParent().getName();
            }
        }
        this.numImages = numImages;
    }
}
