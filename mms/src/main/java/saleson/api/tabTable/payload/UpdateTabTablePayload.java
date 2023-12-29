package saleson.api.tabTable.payload;

import lombok.Data;
import saleson.common.enumeration.ObjectType;
import saleson.model.TabTable;

@Data
public class UpdateTabTablePayload {
    private Long id;
    private Boolean isShow;
    private Boolean deleted;
    private Boolean isDefaultTab;
    private String name;
    private ObjectType objectType;

    public TabTable getTabTable() {
        TabTable tabTable = new TabTable();
        tabTable.setName(this.name);
        tabTable.setDeleted(this.deleted);
        tabTable.setShow(this.isShow);
        tabTable.setIsDefaultTab(this.isDefaultTab);
        tabTable.setObjectType(this.objectType);
        return tabTable;
    }
}
