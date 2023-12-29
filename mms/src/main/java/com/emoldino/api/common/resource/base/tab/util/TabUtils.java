package com.emoldino.api.common.resource.base.tab.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.option.dto.TabConfig;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.framework.dto.Tab;
import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.repository.Qs;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.Closure1Param;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.Property;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ReflectionUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPQLQuery;

import saleson.api.tabTable.TabTableDataRepository;
import saleson.api.tabTable.TabTableRepository;
import saleson.common.enumeration.ObjectType;
import saleson.common.util.SecurityUtils;
import saleson.model.QTabTable;
import saleson.model.QTabTableData;
import saleson.model.TabTable;
import saleson.model.TabTableData;

public class TabUtils {

	public static TabTable findTable(ObjectType objectType, String tabName) {
		LogicUtils.assertNotNull(objectType, "objectType");
		if (ObjectUtils.isEmpty(tabName)) {
			return null;
		}

		QTabTable qTab = QTabTable.tabTable;
		// TODO Because of the Duplicated TabTable Data Bug, call findAll instead of findOne, Temporarily.
//		TabTable tabTable = BeanUtils.get(TabTableRepository.class)//
//				.findOne(qTab.objectType.eq(objectType).and(qTab.name.eq(tabName)).and(qTab.userId.eq(SecurityUtils.getUserId())))//
//				.orElse(null);
		TabTable tabTable = null;//
		for (TabTable item : BeanUtils.get(TabTableRepository.class)//
				.findAll(qTab.objectType.eq(objectType)//
						.and(qTab.name.eq(tabName))//
						.and(qTab.userId.eq(SecurityUtils.getUserId()))//
						.and(qTab.deleted.isNull().or(qTab.deleted.isFalse())), //
						PageRequest.of(0, 1, Direction.DESC, "id")//
				)//
		) {
			tabTable = item;
		}
		return tabTable;
	}

	public static TabTable applyInput(Object input, ObjectType objectType, String tabName) {
		TabTable tabTable = findTable(objectType, tabName);

		// Don't check !isShow and isDeleted

		// Default Tab
		if (tabTable == null || tabTable.getIsDefaultTab()) {
			Map<String, TabConfig> configs = OptionUtils.getDefaultTabConfigs(objectType);
			if (configs == null || !configs.containsKey(tabName)) {
				throw new LogicException("TAB_CONFIG_NOT_FOUND", tabName + " Tab Config of " + objectType.name() + " doesn't exist");
			}
			TabConfig tab = configs.get(tabName);
			if (tab.getInput() != null && input != null) {
				tab.getInput().forEach((fieldName, value) -> ValueUtils.set(input, fieldName, value));
			}
		}

		return tabTable;
	}

	public static void applyFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, TabTable tabTable, NumberPath<Long> idField) {
		if (tabTable == null || tabTable.getIsDefaultTab()) {
			return;
		}

		// Custom Tab
		LogicUtils.assertNotNull(idField, "idField");
		QTabTableData qTabData = QTabTableData.tabTableData;
		QueryUtils.join(query, join, qTabData, qTabData.tabTableId.eq(tabTable.getId()).and(qTabData.refId.eq(idField)));
	}

	public static <I> List<Tab> findAll(ObjectType objectType, I input, Page<?> page, Closure1Param<I, Long> closure) {
		LogicUtils.assertNotNull(objectType, "objectType");
		LogicUtils.assertNotNull(input, "input");
		LogicUtils.assertNotNull(closure, "closure");
		String fieldName = "tabName";
		if (ReflectionUtils.getField(input, fieldName) == null) {
			throw new LogicException("FIELD_NOT_FOUND", new Property("fieldName", fieldName));
		}

		String currentTabName = (String) ValueUtils.get(input, fieldName);
		List<Tab> tabs = new ArrayList<>();
		@SuppressWarnings("unchecked")
		I countin = (I) ValueUtils.map2(input, input.getClass());
		OptionUtils.getUserTabConfigs(objectType).forEach((tabName, tabConfig) -> {
			long totalElements;
			if (page != null && ValueUtils.equals(tabName, currentTabName)) {
				totalElements = page.getTotalElements();
			} else {
				ValueUtils.cleanHiddenFields(countin);
				ValueUtils.set(countin, fieldName, tabName);
				totalElements = closure.execute(countin);
			}
			tabs.add(new Tab(tabName, totalElements, tabConfig.isDefaultTab()));
		});
		return tabs;
	}

	public static void moveAllData(List<Long> ids, TabTable fromTab, TabTable toTab) {
		LogicUtils.assertNotNull(toTab, "toTab");
		if (ObjectUtils.isEmpty(ids)) {
			return;
		}

		List<TabTableData> content = new ArrayList<>();
		List<TabTableData> removeList = new ArrayList<>();
		if (fromTab != null && !ValueUtils.toBoolean(fromTab.getIsDefaultTab(), true)) {
			BeanUtils.get(TabTableDataRepository.class)//
					.findAll(Qs.tabTableData.tabTableId.eq(fromTab.getId()).and(Qs.tabTableData.refId.in(ids)))//
					.forEach(item -> {
						if (BeanUtils.get(TabTableDataRepository.class).exists(Qs.tabTableData.tabTableId.eq(toTab.getId()).and(Qs.tabTableData.refId.eq(item.getRefId())))) {
							removeList.add(item);
							return;
						}
						item.setTabTable(toTab);
						content.add(item);
					});
		} else {
			ids.forEach(id -> {
				if (BeanUtils.get(TabTableDataRepository.class).exists(Qs.tabTableData.tabTableId.eq(toTab.getId()).and(Qs.tabTableData.refId.eq(id)))) {
					return;
				}
				content.add(new TabTableData(id, toTab.getId(), toTab));
			});
		}
		BeanUtils.get(TabTableDataRepository.class).saveAll(content);
		BeanUtils.get(TabTableDataRepository.class).deleteAll(removeList);
	}

	public static void deleteAllData(List<Long> ids, TabTable fromTab) {
		LogicUtils.assertNotNull(fromTab, "fromTab");
		if (ObjectUtils.isEmpty(ids) || fromTab.getIsDefaultTab()) {
			return;
		}

		BeanUtils.get(TabTableDataRepository.class)//
				.deleteAll(BeanUtils.get(TabTableDataRepository.class).findAll(Qs.tabTableData.tabTableId.eq(fromTab.getId()).and(Qs.tabTableData.refId.in(ids))));
	}

}
