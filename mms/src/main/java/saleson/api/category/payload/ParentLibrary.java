package saleson.api.category.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Category;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParentLibrary {
    private Long id;
    private String name;
    private Long parentId;
    private String parentName;
    private Long grandParentId;
    private String grandParentName;
    private List<ChildProfile> childProfiles = new ArrayList<>();

    public ParentLibrary(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        if (category.getParent() != null) {
            this.parentId = category.getParent().getId();
            this.parentName = category.getParent().getName();
        }
        if (category.getGrandParent()!=null){
            this.grandParentId = category.getGrandParent().getId();
            this.grandParentName = category.getGrandParent().getName();
        }
    }
}
