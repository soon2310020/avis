package saleson.api.versioning.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import saleson.api.category.CategoryService;
import saleson.api.versioning.repositories.CategoryVersionRepository;
import saleson.api.versioning.repositories.ReversionHistoryRepository;
import saleson.common.enumeration.RevisionObjectType;
import saleson.common.util.SecurityUtils;
import saleson.model.Category;
import saleson.model.clone.CategoryVersion;
import saleson.model.clone.RevisionHistory;

@Service
public class CategoryVersionService {

    @Autowired
    CategoryVersionRepository categoryVersionRepository;

    @Autowired
    ReversionHistoryRepository reversionHistoryRepository;

    @Autowired
    CategoryService categoryService;

    private CategoryVersion clone(Category category){
        CategoryVersion categoryVersion = CategoryVersion.builder()
                .name(category.getName())
                .description(category.getDescription())
                .enabled(category.isEnabled())
                .parentId(category.getParent() != null ? category.getParent().getId() :  null)
                .build();
        categoryVersion.setOriginId(category.getId());

        return categoryVersion;
    }

/*
    public Category convertToCategory(CategoryVersion categoryVersion, Category category){
        if(categoryVersion == null) return null;

        if(category == null) category = new Category();
        else category.setId(categoryVersion.getOriginId());

        category.setName(categoryVersion.getName());
        category.setDescription(categoryVersion.getDescription());
        category.setEnabled(categoryVersion.isEnabled());

        Category categoryParent = new Category();
        categoryParent.setId(categoryVersion.getParentId());

        category.setParent(categoryParent);

        return category;
    }
*/

    public CategoryVersion writeHistory(Category category){
        CategoryVersion categoryVersion = categoryVersionRepository.save(clone(category));
        RevisionHistory revisionHistory = new RevisionHistory();
        revisionHistory.setRevisionId(category.getId());
        revisionHistory.setEditedBy(SecurityUtils.getUserId());
        revisionHistory.setRevisionObjectType(RevisionObjectType.CATEGORY);
        reversionHistoryRepository.save(revisionHistory);

        return categoryVersion;
    }

/*
    public void updateAndWriteHistory(CategoryVersion categoryVersion){
        if(categoryVersion == null || categoryVersion.getOriginId() == null) return;
        Category category = categoryService.findById(categoryVersion.getOriginId());
        writeHistory(category);
        categoryService.save(convertToCategory(categoryVersion, category));
    }
*/
}
