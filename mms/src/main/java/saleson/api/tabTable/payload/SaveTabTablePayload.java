package saleson.api.tabTable.payload;

import lombok.Data;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

@Data
public class SaveTabTablePayload {
    private Long id;
    private String name;
    private Boolean isDuplicate;
    private List<Long> removeItemIdList = Lists.newArrayList();
    private List<Long> addItemIdList = Lists.newArrayList();
}
