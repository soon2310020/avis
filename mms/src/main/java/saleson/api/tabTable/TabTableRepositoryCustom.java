package saleson.api.tabTable;

import saleson.common.enumeration.ObjectType;
import saleson.model.TabTable;

import java.util.List;

public interface TabTableRepositoryCustom {

    List<TabTable> findAllByUserIdAndDeletedFalseAndObjectType(Long userId, ObjectType objectType);
}
