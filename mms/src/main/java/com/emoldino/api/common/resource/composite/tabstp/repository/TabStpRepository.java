package com.emoldino.api.common.resource.composite.tabstp.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.option.dto.TabConfig;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.api.common.resource.composite.tabstp.dto.TabStpData;
import com.emoldino.api.common.resource.composite.tabstp.dto.TabStpGetIn;
import com.emoldino.api.common.resource.composite.tabstp.dto.TabStpItem;
import com.emoldino.api.common.resource.composite.tabstp.dto.TabStpPostData;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.api.tabTable.TabTableDataRepository;
import saleson.api.tabTable.TabTableRepository;
import saleson.common.enumeration.ObjectType;
import saleson.common.util.SecurityUtils;
import saleson.model.QTabTable;
import saleson.model.QTabTableData;
import saleson.model.TabTable;
import saleson.model.TabTableData;

@Repository
public class TabStpRepository {

	public ListOut<TabStpItem> findAll(ObjectType objectType, TabStpGetIn input) {
		LogicUtils.assertNotNull(objectType, "objectType");

		Map<String, TabStpItem> content = new LinkedHashMap<>();

		Map<String, TabConfig> tabs = OptionUtils.getDefaultTabConfigs(objectType);
		tabs.forEach((name, item) -> content.put(name, new TabStpItem(null, name, true, true)));

		QTabTable qTabTable = QTabTable.tabTable;

		JPQLQuery<TabStpItem> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(TabStpItem.class, //
						qTabTable.id, //
						qTabTable.name, //
						qTabTable.isDefaultTab, //
						qTabTable.isShow//
				))//
				.from(qTabTable)//
				.where(//
						qTabTable.objectType.eq(objectType)//
								.and(qTabTable.userId.eq(SecurityUtils.getUserId()))//
								.and(qTabTable.deleted.isNull().or(qTabTable.deleted.isFalse())));

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
//				.put("id", qTabTable.id)//
//				.put("name", qTabTable.name)//
//				.put("objectType", qTabTable.objectType)//
//				.put("isDefaultTab", qTabTable.isDefaultTab)//
				.build();

		QueryUtils.applySort(query, Sort.unsorted(), fieldMap, qTabTable.id.asc());

		query.fetch().forEach(item -> {
			if (content.containsKey(item.getName())) {
				if (item.isDefaultTab()) {
					if (!item.isShown()) {
						TabStpItem defaultItem = content.get(item.getName());
						defaultItem.setShown(false);
					}
					return;
				}
				for (int i = 1; i < 1000; i++) {
					String name = item.getName() + " (" + i + ")";
					if (content.containsKey(name)) {
						continue;
					}
					item.setName(name);
					TabTable data = findTabTableById(objectType, item.getId());
					data.setName(name);
					BeanUtils.get(TabTableRepository.class).save(data);
					break;
				}
			}
			content.put(item.getName(), new TabStpItem(item.getId(), item.getName(), false, item.isShown()));
		});

		return new ListOut<>(new ArrayList<>(content.values()));
	}

	public TabStpData findById(ObjectType objectType, Long id) {
		TabTable tabTable = findTabTableById(objectType, id);

		TabStpData data = TabStpData.builder()//
				.id(tabTable.getId())//
				.name(tabTable.getName())//
				.defaultTab(ValueUtils.toBoolean(tabTable.getIsDefaultTab(), false))//
				.shown(ValueUtils.toBoolean(tabTable.isShow(), true))//
				.build();

		List<Long> selectedIds = new ArrayList<>();
		QTabTableData qTabTableData = QTabTableData.tabTableData;
		BeanUtils.get(TabTableDataRepository.class)//
				.findAll(qTabTableData.tabTableId.eq(id))//
				.forEach(item -> selectedIds.add(item.getRefId()));
		data.setSelectedIds(selectedIds);

		return data;
	}

	public TabStpData save(ObjectType objectType, TabStpPostData data) {
		LogicUtils.assertNotNull(objectType, "objectType");
		ValueUtils.assertNotEmpty(data.getName(), "name");

		QTabTable qTabTable = QTabTable.tabTable;
		if (BeanUtils.get(TabTableRepository.class).exists(//
				qTabTable.objectType.eq(objectType)//
						.and(qTabTable.userId.eq(SecurityUtils.getUserId()))//
						.and(qTabTable.name.eq(data.getName()))//
		)) {
			for (int i = 1; i < 1000; i++) {
				String name = data.getName() + " (" + i + ")";
				if (BeanUtils.get(TabTableRepository.class).exists(//
						qTabTable.objectType.eq(objectType)//
								.and(qTabTable.userId.eq(SecurityUtils.getUserId()))//
								.and(qTabTable.name.eq(name))//
				)) {
					continue;
				}
				data.setName(name);
				break;
			}
		}

		TabTable tabTable = new TabTable(data.getName(), SecurityUtils.getUserId(), objectType, false);
		BeanUtils.get(TabTableRepository.class).save(tabTable);

		if (!ObjectUtils.isEmpty(data.getSelectedIds())) {
			data.getSelectedIds().forEach(refId -> {
				BeanUtils.get(TabTableDataRepository.class).save(new TabTableData(refId, tabTable.getId(), tabTable));
			});
		}

		return TabStpData.builder()//
				.id(tabTable.getId())//
				.name(tabTable.getName())//
				.defaultTab(tabTable.getIsDefaultTab())//
				.shown(tabTable.isShow())//
				.selectedIds(data.getSelectedIds())//
				.build();
	}

	public TabStpData save(ObjectType objectType, Long id, TabStpPostData data) {
		LogicUtils.assertNotNull(objectType, "objectType");
		LogicUtils.assertNotNull(id, "id");
		LogicUtils.assertNotNull(data, "data");
		ValueUtils.assertNotEmpty(data.getName(), "name");

		TabTable tabTable = findTabTableById(objectType, id);

		QTabTable qTabTable = QTabTable.tabTable;
		if (BeanUtils.get(TabTableRepository.class).exists(//
				qTabTable.objectType.eq(objectType)//
						.and(qTabTable.userId.eq(SecurityUtils.getUserId()))//
						.and(qTabTable.name.eq(data.getName()))//
						.and(qTabTable.id.ne(id))//
		)) {
			for (int i = 1; i < 1000; i++) {
				String name = data.getName() + " (" + i + ")";
				if (BeanUtils.get(TabTableRepository.class).exists(//
						qTabTable.objectType.eq(objectType)//
								.and(qTabTable.userId.eq(SecurityUtils.getUserId()))//
								.and(qTabTable.name.eq(name))//
								.and(qTabTable.id.ne(id))//
				)) {
					continue;
				}
				data.setName(name);
				break;
			}
		}

		tabTable.setName(data.getName());
		BeanUtils.get(TabTableRepository.class).save(tabTable);

		Map<Long, TabTableData> selected = new LinkedHashMap<>();
		QTabTableData qTabTableData = QTabTableData.tabTableData;
		BeanUtils.get(TabTableDataRepository.class)//
				.findAll(qTabTableData.tabTableId.eq(id))//
				.forEach(item -> selected.put(item.getRefId(), item));
		if (!ObjectUtils.isEmpty(data.getSelectedIds())) {
			data.getSelectedIds().forEach(selectedId -> {
				if (selected.containsKey(selectedId)) {
					selected.remove(selectedId);
				} else {
					BeanUtils.get(TabTableDataRepository.class).save(new TabTableData(selectedId, tabTable.getId(), tabTable));
				}
			});
		}
		BeanUtils.get(TabTableDataRepository.class).deleteAll(selected.values());

		return TabStpData.builder()//
				.id(tabTable.getId())//
				.name(tabTable.getName())//
				.defaultTab(tabTable.getIsDefaultTab())//
				.shown(tabTable.isShow())//
				.selectedIds(data.getSelectedIds())//
				.build();
	}

	public void hidden(ObjectType objectType, Long id) {
		TabTable tabTable = findTabTableById(objectType, id);
		tabTable.setShow(false);
		BeanUtils.get(TabTableRepository.class).save(tabTable);
	}

	public void shown(ObjectType objectType, Long id) {
		TabTable tabTable = findTabTableById(objectType, id);
		tabTable.setShow(true);
		BeanUtils.get(TabTableRepository.class).save(tabTable);
	}

	public void hiddenByName(ObjectType objectType, String name) {
		findTabTableByName(objectType, name).forEach(tabTable -> {
			tabTable.setShow(false);
			BeanUtils.get(TabTableRepository.class).save(tabTable);
		});
	}

	public void shownByName(ObjectType objectType, String name) {
		findTabTableByName(objectType, name).forEach(tabTable -> {
			tabTable.setShow(true);
			BeanUtils.get(TabTableRepository.class).save(tabTable);
		});
	}

	public void delete(ObjectType objectType, Long id) {
		TabTable tabTable = findTabTableById(objectType, id);
		QTabTableData qTabTableData = QTabTableData.tabTableData;
		BeanUtils.get(TabTableDataRepository.class).deleteAll(//
				BeanUtils.get(TabTableDataRepository.class)//
						.findAll(qTabTableData.tabTableId.eq(id))//
		);
		BeanUtils.get(TabTableRepository.class).delete(tabTable);
	}

	private TabTable findTabTableById(ObjectType objectType, Long id) {
		LogicUtils.assertNotNull(objectType, "objectType");
		LogicUtils.assertNotNull(id, "id");

		TabTable tabTable = BeanUtils.get(TabTableRepository.class).findById(id).orElse(null);
		if (tabTable == null || !tabTable.getObjectType().equals(objectType) || !tabTable.getUserId().equals(SecurityUtils.getUserId())) {
			throw DataUtils.newDataNotFoundException(TabTable.class, "id", id);
		}
		return tabTable;
	}

	private List<TabTable> findTabTableByName(ObjectType objectType, String name) {
		LogicUtils.assertNotNull(objectType, "objectType");
		LogicUtils.assertNotNull(name, "name");
		Long userId = SecurityUtils.getUserId();

		List<TabTable> list = new ArrayList<>();
		QTabTable qTabTable = QTabTable.tabTable;
		BeanUtils.get(TabTableRepository.class).findAll(//
				qTabTable.objectType.eq(objectType)//
						.and(qTabTable.userId.eq(userId))//
						.and(qTabTable.name.eq(name))//
		).forEach(tabTable -> list.add(tabTable));

		if (list.isEmpty() && OptionUtils.getDefaultTabConfigs(objectType).containsKey(name)) {
			list.add(new TabTable(name, userId, objectType, true));
		}

		return list;
	}

}
