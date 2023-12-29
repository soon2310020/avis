package saleson.api.tabTable;

import saleson.common.enumeration.ObjectType;
import saleson.model.TabTableData;

import java.util.List;

public interface TabTableDataRepositoryCustom {
    List<Long> findAllRefIdByTabTableId(Long tabTableId);

    List<TabTableData> findAllByTabTableId(Long tabTableId);
}
