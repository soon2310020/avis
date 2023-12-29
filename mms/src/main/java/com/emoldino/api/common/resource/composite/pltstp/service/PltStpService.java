package com.emoldino.api.common.resource.composite.pltstp.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import com.emoldino.api.common.resource.base.tab.util.TabUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.location.repository.area.Area;
import com.emoldino.api.common.resource.base.location.repository.area.AreaRepository;
import com.emoldino.api.common.resource.base.location.repository.area.QArea;
import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.base.note.util.NoteUtils;
import com.emoldino.api.common.resource.base.object.util.EmObjectUtils;
import com.emoldino.api.common.resource.composite.datexp.util.DatExpUtils;
import com.emoldino.api.common.resource.composite.pltstp.dto.PltStpArea;
import com.emoldino.api.common.resource.composite.pltstp.dto.PltStpGetIn;
import com.emoldino.api.common.resource.composite.pltstp.dto.PltStpGetOut;
import com.emoldino.api.common.resource.composite.pltstp.dto.PltStpItem;
import com.emoldino.api.common.resource.composite.pltstp.repository.PltStpRepository;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.ListIn;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.dto.Tab;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.Closure1ParamNoReturn;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.emoldino.framework.util.LogicUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.api.location.LocationRepository;
import saleson.api.location.LocationService;
import saleson.api.versioning.service.VersioningService;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.PageType;
import saleson.model.Location;
import saleson.model.TabTable;

@Service
public class PltStpService {

	@Transactional
	public PltStpGetOut get(PltStpGetIn input, Pageable pageable) {
		Page<PltStpItem> page = get(input, null, pageable);
		page.forEach(plant -> plant.setAreas(getAreas(plant.getId()).getContent()));
		List<Tab> tabs = QueryUtils.findTabs(ObjectType.LOCATION, input, page, //
				countin -> BeanUtils.get(PltStpRepository.class).count(countin));
		return new PltStpGetOut(page, tabs);
	}

	private Page<PltStpItem> get(PltStpGetIn input, BatchIn batchin, Pageable pageable) {
		Page<PltStpItem> page = BeanUtils.get(PltStpRepository.class).findAll(input, batchin, pageable);
		loadCustomFieldValues(page);
		return page;
	}

	private void loadCustomFieldValues(Page<PltStpItem> page) {
		Map<Long, PltStpItem> map = page.getContent().stream().collect(Collectors.toMap(PltStpItem::getId, Function.identity(), (v1, v2) -> v1, LinkedHashMap::new));
		EmObjectUtils.loadCustomFieldValues(ObjectType.LOCATION, new ArrayList<>(map.keySet()), (objectId, fields) -> map.get(objectId).setCustomFields(fields));
	}

	@Transactional
	public ListOut<PltStpArea> getAreas(Long locationId) {
		List<PltStpArea> list = ValueUtils.toList(findByLocationId(locationId), PltStpArea.class);
		return new ListOut<>(list);
	}

	@Transactional
	public void postAreas(Long locationId, ListIn<PltStpArea> input) {
		ValueUtils.assertNotEmpty(locationId, "location_id");

		List<Area> areas = new ArrayList<>();
		Map<Long, Area> map = new LinkedHashMap<>();
		findByLocationId(locationId).forEach(item -> map.put(item.getId(), item));
		if (!ObjectUtils.isEmpty(input.getContent())) {
//			int position = 0;
			for (PltStpArea item : input.getContent()) {
				Area area;
				if (item.getId() != null) {
					if (!map.containsKey(item.getId())) {
						if (BeanUtils.get(AreaRepository.class).existsById(item.getId())) {
							throw new BizException("LOCATION_DIFFERENT");
						} else {
							throw DataUtils.newDataNotFoundException(Area.class, "id", item.getId());
						}
					}
					area = map.remove(item.getId());
					ValueUtils.map(item, area);
				} else {
					area = ValueUtils.map(item, Area.class);
					area.setLocationId(locationId);
				}
				area.setPosition(0);
//				area.setPosition(++position);
				areas.add(area);
			}
		}

		BeanUtils.get(AreaRepository.class).deleteAll(map.values());
		BeanUtils.get(AreaRepository.class).saveAll(areas);
	}

	private Iterable<Area> findByLocationId(Long locationId) {
		ValueUtils.assertNotEmpty(locationId, "location_id");
		QArea table = QArea.area;
		BooleanBuilder filter = new BooleanBuilder().and(table.locationId.eq(locationId));
		Iterable<Area> iter = BeanUtils.get(AreaRepository.class).findAll(filter, Sort.by(Direction.ASC, "position", "name"));
		return iter;
	}

//	private Area getArea(Long locationId, Long id) {
//		Area area = BeanUtils.get(AreaRepository.class).findById(id).orElseThrow(() -> DataUtils.newDataNotFoundException(Area.class, "id", id));
//		if (!locationId.equals(area.getLocationId())) {
//			throw new BizException("LOCATION_DIFFERENT");
//		}
//		return area;
//	}
//
//	public void postArea(Long locationId, PltStpAreaSave item) {
//		ValueUtils.assertNotEmpty(locationId, "location_id");
//		Area area = ValueUtils.map(item, Area.class);
//		area.setLocationId(locationId);
//		BeanUtils.get(AreaRepository.class).save(area);
//	}
//
//	public void putArea(Long locationId, Long areaId, PltStpAreaSave item) {
//		ValueUtils.assertNotEmpty(locationId, "location_id");
//		ValueUtils.assertNotEmpty(areaId, "areaId");
//		Area area = get(locationId, areaId);
//		ValueUtils.map(item, area);
//		BeanUtils.get(AreaRepository.class).save(area);
//	}
//
//	public void deleteArea(Long locationId, Long areaId) {
//		ValueUtils.assertNotEmpty(locationId, "location_id");
//		ValueUtils.assertNotEmpty(areaId, "areaId");
//		Area area = get(locationId, areaId);
//		BeanUtils.get(AreaRepository.class).delete(area);
//	}

	@Transactional
	public void disable(List<Long> ids) {
		saveEnabled(ids, false);
	}

	@Transactional
	public void enable(List<Long> ids) {
		saveEnabled(ids, true);
	}

	private void saveEnabled(List<Long> ids, boolean enabled) {
		if (ObjectUtils.isEmpty(ids) || ids.size() > 100) {
			return;
		}
		for (Long id : ids) {
			Location data = findById(id);
			data.setEnabled(enabled);
			save(data);
		}
	}

	private Location findById(Long id) {
		return BeanUtils.get(LocationRepository.class).findById(id).orElseThrow(() -> DataUtils.newDataNotFoundException(Location.class, "id", id));
	}

	private void save(Location data) {
		BeanUtils.get(LocationService.class).save(data);
		Location dataGet = BeanUtils.get(LocationService.class).findById(data.getId());
		BeanUtils.get(VersioningService.class).writeHistory(dataGet);
	}

	public void disableBatch(PltStpGetIn input, BatchIn batchin) {
		saveEnabledBatch(input, batchin, false);
	}

	public void enableBatch(PltStpGetIn input, BatchIn batchin) {
		saveEnabledBatch(input, batchin, true);
	}

	private void saveEnabledBatch(PltStpGetIn input, BatchIn batchin, Boolean enabled) {
		ValueUtils.assertNotEmpty(enabled, "enabled");

		runBatch(input, batchin, item -> {
			Location location = findById(item.getId());
			location.setEnabled(enabled);
			save(location);
		});
	}

	public void postNoteBatch(PltStpGetIn input, BatchIn batchin, NoteIn body) {
		runBatch(input, batchin, item -> NoteUtils.post(PageType.LOCATION, item.getId(), body));
	}

	public void runBatch(PltStpGetIn input, BatchIn batchin, Closure1ParamNoReturn<PltStpItem> closure) {
		DataUtils.runBatch(Sort.unsorted(), 100, true, //
				pageable -> BeanUtils.get(PltStpRepository.class).findAll(input, batchin, pageable), //
				item -> TranUtils.doNewTran(() -> closure.execute(item))//
		);
	}

	@Transactional
	public void export(PltStpGetIn input, BatchIn batchin, Sort sort, HttpServletResponse response) {
		DatExpUtils.exportByJxls("PLANT", //
				pageable -> BeanUtils.get(PltStpService.class).get(input, batchin, pageable), //
				100, sort == null || sort.isUnsorted() ? Sort.by("locationCode") : sort, //
				"Plant", response//
		);
	}

	public void deleteTabItemsBatch(PltStpGetIn input, BatchIn batchin) {
		LogicUtils.assertNotNull(input.getTabName(), "tabName");
		TabTable fromTab = QueryUtils.findTabTable(ObjectType.LOCATION, input.getTabName());
		runContentBatch(input, batchin, itemIds -> TabUtils.deleteAllData(itemIds, fromTab), false);
	}

	private void runContentBatch(PltStpGetIn input, BatchIn batchin, Closure1ParamNoReturn<List<Long>> closure, boolean pageUpRequired) {
		DataUtils.runContentBatch(Sort.unsorted(), 100, pageUpRequired, //
				pageable -> BeanUtils.get(PltStpRepository.class).findAll(input, batchin, pageable), //
				items -> TranUtils.doNewTran(() -> closure.execute(items.stream().map(ob -> ob.getId()).collect(Collectors.toList())))//
		);
	}

	public void moveTabItemsBatch(PltStpGetIn input, BatchIn batchin, String toTabName) {
		LogicUtils.assertNotNull(input.getTabName(), "tabName");
		TabTable fromTab = QueryUtils.findTabTable(ObjectType.LOCATION, input.getTabName());
		TabTable toTab = QueryUtils.findTabTable(ObjectType.LOCATION, toTabName);
		if (toTab == null) {
			return;
		}
		runContentBatch(input, batchin, itemIds -> TabUtils.moveAllData(itemIds, fromTab, toTab), false);
	}

}
