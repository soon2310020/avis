package saleson.api.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.option.dto.TableColumnConfigItem;
import com.emoldino.api.common.resource.base.option.service.OptionService;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import saleson.api.configuration.payload.ColumnTableConfigPayload;
import saleson.common.enumeration.PageType;
import saleson.model.checklist.ChecklistType;

@Service
public class ColumnTableConfigService {

	public List<TableColumnConfigItem> getColumnConfig(PageType pageType) {
		ValueUtils.assertNotEmpty(pageType, "pageType");
		List<TableColumnConfigItem> list = OptionUtils.getUserContent("TABLE_COLUMN-" + pageType.name(), new TypeReference<List<TableColumnConfigItem>>() {
		});
		return list;
	}

	public List<TableColumnConfigItem> updateColumnConfig(ColumnTableConfigPayload payload) {
		PageType pageType = payload.getPageType();
		ValueUtils.assertNotEmpty(pageType, "pageType");
		List<TableColumnConfigItem> list = payload.getColumnConfig() == null ? new ArrayList<>() : payload.getColumnConfig();
		BeanUtils.get(OptionService.class).saveUserContent("TABLE_COLUMN-" + pageType.name(), list);
		return list;
	}

	public List<String> getSelectedFields(PageType type) {
		switch (type) {
		case COMPANY_SETTING:
			return getSelectedFields(type, "companyCode", "name");
		case CATEGORY_SETTING:
			return getSelectedFields(type, "name", "description");
		case PART_SETTING:
			return getSelectedFields(type, "category", "projectName");
		case TOOLING_SETTING:
		case RELOCATION_ALERT:
		case CYCLE_TIME_ALERT:
		case DOWNTIME_ALERT:
		case MAINTENANCE_ALERT:
			return getSelectedFields(type, "company", "location");
		case MACHINE_SETTING:
			return getSelectedFields(type, "company", "location", "line", "machineMaker", "machineType", "machineModel");
		case COUNTER_SETTING:
			return getSelectedFields(type, "mold.equipmentCode", "company", "location", "batteryStatus", "installedBy", "memo");
		case TERMINAL_SETTING:
			return getSelectedFields(type, "company", "location", "installationArea", "installedBy", "memo");
		case CHECKLIST_MAINTENANCE:
		case CHECKLIST_REJECT_RATE:
		case CHECKLIST_GENERAL:
		case CHECKLIST_REFURBISHMENT:
		case CHECKLIST_DISPOSAL:
			return getSelectedFields(type, "companyName", "companyId", "address");
		default:
			return new ArrayList<>();
		}
	}

	private List<String> getSelectedFields(PageType pageType, String... defaultFieldNames) {
		List<String> selectedFields = getColumnConfig(pageType).stream()//
				.filter(item -> ValueUtils.toBoolean(item.getEnabled(), false))//
				.map(TableColumnConfigItem::getColumnName)//
				.collect(Collectors.toList());
		if (ObjectUtils.isEmpty(selectedFields)) {
			selectedFields = Arrays.asList(defaultFieldNames);
		}
		return selectedFields;
	}

	public List<String> getListManagementSelectedFields(ChecklistType checklistType) {
		if (checklistType == null) {
			return getSelectedFields(PageType.CHECKLIST_MAINTENANCE);
		}

		switch (checklistType) {
		case REJECT_RATE:
			return getSelectedFields(PageType.CHECKLIST_REJECT_RATE);
		case GENERAL:
			return getSelectedFields(PageType.CHECKLIST_GENERAL);
		case REFURBISHMENT:
			return getSelectedFields(PageType.CHECKLIST_REFURBISHMENT);
		case DISPOSAL:
			return getSelectedFields(PageType.CHECKLIST_DISPOSAL);
        case QUALITY_ASSURANCE:
            return getSelectedFields(PageType.CHECKLIST_QUALITY_ASSURANCE);
            default:
			return getSelectedFields(PageType.CHECKLIST_MAINTENANCE);
		}
	}
}
