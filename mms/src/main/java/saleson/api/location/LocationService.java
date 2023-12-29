package saleson.api.location;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.MessageUtils;
import com.emoldino.framework.util.ValueUtils;
import com.google.gson.JsonObject;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import saleson.api.company.CompanyRepository;
import saleson.api.dataCompletionRate.DataCompletionRateService;
import saleson.api.dataRequest.DataRequestService;
import saleson.api.location.payload.LocationPayload;
import saleson.api.mold.MoldRepository;
import saleson.api.tabTable.TabTableDataRepository;
import saleson.api.tabTable.TabTableRepository;
import saleson.api.terminal.TerminalRepository;
import saleson.api.versioning.service.VersioningService;
import saleson.common.constant.CommonMessage;
import saleson.common.enumeration.ContinentName;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.StorageType;
import saleson.common.payload.ApiResponse;
import saleson.common.service.FileInfo;
import saleson.common.service.FileStorageService;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.dto.BatchUpdateDTO;
import saleson.model.*;
import saleson.model.data.CountLocationMold;
import saleson.model.data.TerminalLocationCode;
import saleson.model.data.LocationData;
import saleson.model.data.MiniComponentData;
import saleson.service.util.ContinentListUtils;

@Service
public class LocationService {

	@Autowired
	LocationRepository locationRepository;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	TerminalRepository terminalRepository;

	@Autowired
	ContinentRepository continentRepository;

	@Autowired
	VersioningService versioningService;
	@Autowired
	DataCompletionRateService dataCompletionRateService;

	@Autowired
	private MoldRepository moldRepository;

	@Autowired
	private DataRequestService dataRequestService;
	@Autowired
	private TabTableDataRepository tabTableDataRepository;

	@Autowired
	private TabTableRepository tabTableRepository;

	public Page<Location> findAll(Predicate predicate, Pageable pageable) {
		return locationRepository.findAll(predicate, pageable);
	}

	private List<MiniComponentData> getMiniCommponetDataWithFilter(List<Location> locations) {
		//filter by company tree
		if (AccessControlUtils.isAccessFilterRequired()) {
			List<Long> companyIdAccessList = AccessControlUtils.getAllAccessibleCompanyIds();
			if (!companyIdAccessList.isEmpty())
				locations = locations.stream().filter(location -> companyIdAccessList.contains(location.getCompanyId())).collect(Collectors.toList());
		}
		List<MiniComponentData> componentData = locations.stream().map(x -> {
			MiniComponentData component = new MiniComponentData();
			component.setId(x.getId());
			component.setName(x.getName());
			return component;
		}).collect(Collectors.toList());
		return componentData;
	}

	public List<MiniComponentData> findAllMiniData() {
		List<Location> locations = locationRepository.findAll();
		locations = locations.stream().filter(l -> !l.getCompany().isEmoldino()).collect(Collectors.toList());
		return getMiniCommponetDataWithFilter(locations);
	}

	public List<MiniComponentData> findAllMiniDataFiltered(boolean isAll) {
		List<Location> locations = locationRepository.findAllByGeneralFilter(isAll);
		return getMiniCommponetDataWithFilter(locations);
	}

	public List<MiniComponentData> findAllMiniDataFilteredCustom(DashboardGeneralFilter filter) {
		List<Location> locations = locationRepository.findAllByGeneralFilter(filter);
		return getMiniCommponetDataWithFilter(locations);
	}

	public Location findById(Long id) {
		Optional<Location> optional = locationRepository.findById(id);
		return optional.orElse(null);
	}

	@Transactional
	public void save(Location location) {
		save(location, null);
	}

	@Transactional
	public Location save(Location location, Long dataRequestId) {
		Optional<Company> data = companyRepository.findById(location.getCompanyId());
		if (data.isPresent()) {
			location.setCompany(data.get());
		}

		boolean objNew = location.getId() == null;

		Location locationNew = locationRepository.save(location);

		if (location.getCountryCode() != null && location.getCountryCode() != "") {
			String countryCode = location.getCountryCode();
			ContinentName continentName = ContinentName.EMEA;
			if (ContinentListUtils.asiaCountries.contains(countryCode)) {
				continentName = ContinentName.ASIA;
			} else if (ContinentListUtils.southAmericaCountries.contains(countryCode)) {
				continentName = ContinentName.SOUTH_AMERICA;
			} else if (ContinentListUtils.northAmericaCountries.contains(countryCode)) {
				continentName = ContinentName.NORTH_AMERICA;
			} else if (ContinentListUtils.oceaniaCountries.contains(countryCode)) {
				continentName = ContinentName.OCEANIA;
			}
			Continent continent = continentRepository.findByContinentNameAndCountryCode(continentName, countryCode).orElse(null);
			if (continent == null) {
				continentRepository.save(new Continent(continentName, countryCode));
			}
		}
		//		Write history
		if (objNew) {
			versioningService.writeHistory(locationNew);
		} else {
			dataRequestService.completeDataCompletion(location.getId(), ObjectType.LOCATION);
		}

		if (dataRequestId != null) {
			dataRequestService.saveDataRequestObject(dataRequestId, locationNew.getId(), ObjectType.LOCATION);
		}

		//update data completion rate
		dataCompletionRateService.updateCompanyCompletionRateByObjectType(ObjectType.LOCATION, location.getCompanyId());
		//update data completion order
		dataCompletionRateService.updateCompletionOrder(SecurityUtils.getUserId(), ObjectType.LOCATION, location.getId());

		return locationNew;
	}

	@Transactional
	public void saveMultipart(Location location, LocationPayload payload) {
		save(location);
		if (payload.getFiles() != null) {
			BeanUtils.get(FileStorageService.class).save(new FileInfo(StorageType.LOCATION_PICTURE, location.getId(), payload.getFiles()));
		}
	}

	@Transactional
	public void deleteById(Long id) {

		Optional<Location> optional = locationRepository.findById(id);

		if (optional.isPresent()) {
			Location location = optional.get();

			boolean exists = false;

			exists = terminalRepository.existsByLocationId(location.getId());

			if (exists) {
				throw new RuntimeException("You can not delete it because it has a location in use.");
			} else {
				locationRepository.deleteById(id);
				//update data completion rate
				dataCompletionRateService.updateCompanyCompletionRateByObjectType(ObjectType.LOCATION, location.getCompanyId());
			}
		}
	}

	public Page<Location> findLocationData(Predicate predicate, Pageable pageable, List<String> locationCodeList) {
		List<LocationData> locationDataList = locationRepository.findLocationData(predicate, pageable, locationCodeList);
		List<Location> locationList = locationDataList.stream().map(locationData -> LocationData.convertToModel(locationData)).collect(Collectors.toList());
		return new PageImpl<>(locationList, pageable, locationRepository.count(predicate));
	}
	public List<JSONObject>  findAllIds(LocationPayload payload)
	{
		changeTabPayload(payload);
		BooleanBuilder builder = new BooleanBuilder(payload.getPredicate());
		JPAQuery<Tuple> query = BeanUtils.get(JPAQueryFactory.class)
				.select( Q.location.id,Q.location.name,Q.location.locationCode)
				.from(Q.location);
		query.where(builder);
		QueryResults<Tuple> results = query.fetchResults();
	List<JSONObject> result = new ArrayList<>() ;
		for (Tuple tuple : results.getResults()) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("id", tuple.get(Q.location.id));
			jsonObject.put("locationCode", tuple.get(Q.location.locationCode));
			jsonObject.put("locationTitle",tuple.get(Q.location.name));
			result.add(jsonObject);
			
		}
		return result;
	}



	public List<TerminalLocationCode> getTerminalIdsByLocationId(Long id) {
		Location location = findById(id);
		if (location == null) {
			return new ArrayList<>();
		}
		List<Terminal> terminals = terminalRepository.findAllByLocationId(id);
		return terminals.stream().map(terminal -> new TerminalLocationCode(terminal.getEquipmentCode(), location.getLocationCode(), terminal.getInstallationArea()))
				.collect(Collectors.toList());

	}

    public boolean existsLocationCode(String locationCode, Long id) {
        if (id != null) {
            return locationRepository.existsLocationsByLocationCodeAndIdNot(locationCode, id);
        }
        return locationRepository.existsLocationsByLocationCode(locationCode);
    }

    public boolean existsLocationName(String name, Long id) {
        if (id != null) {
            return locationRepository.existsLocationsByNameAndIdNot(name, id) ;
        }
        return locationRepository.existsLocationsByName(name);
    }

	public ApiResponse validLocation(LocationPayload payload, Long id) {
		ValueUtils.assertNotEmpty(payload.getCompanyId(), "company");

		ValueUtils.assertNotEmpty(payload.getName(), "location_name");
		ValueUtils.assertNotEmpty(payload.getLocationCode(), "location_code");

		payload.setLocationCode(StringUtils.trimWhitespace(payload.getLocationCode()));
		payload.setName(StringUtils.trimWhitespace(payload.getName()));
		boolean existsCode = existsLocationCode(payload.getLocationCode(), id);
		boolean existsName = existsLocationName(payload.getName(), id);

		if (existsCode) {
			return new ApiResponse(false, "Plant ID is already registered in the system.");
		}
		if (existsName) {
			return new ApiResponse(false, "Plant Name is already registered in the system.");
		}
		if (!ObjectUtils.isEmpty(payload.getTimeZoneId())) {
			try {
				ZoneId.of(payload.getTimeZoneId());
			} catch (DateTimeException e) {
				throw DataUtils.newDataValueInvalidException(Location.class, MessageUtils.get("timezone", null), payload.getTimeZoneId());
//				return new ApiResponse(false, e.getMessage());
			}
		}
		ValueUtils.assertNotEmpty(payload.getAddress(), "address");
		return null;
	}

	public ApiResponse changeStatusInBatch(BatchUpdateDTO dto) {
		try {
			List<Location> locations = locationRepository.findAllById(dto.getIds());
			locations.forEach(location -> {
				;
				location.setEnabled(dto.isEnabled());
				save(location);
				Location locationFinal = findById(location.getId());
				versioningService.writeHistory(locationFinal);
			});
			return ApiResponse.success(CommonMessage.OK, locations);
		} catch (Exception e) {
			return ApiResponse.error(e.getMessage());
		}
	}

	public ApiResponse getMoldNumber() {
		List<CountLocationMold> countLocationMolds = moldRepository.countLocationMold();
		return ApiResponse.success(CommonMessage.OK, countLocationMolds);
	}

	public void changeTabPayload(LocationPayload payload) {
		payload.setIsDefaultTab(true);
		if (payload.getTabId() != null) {
			Optional<TabTable> tabTableOptional = tabTableRepository.findById(payload.getTabId());
			if (tabTableOptional.isPresent() && (tabTableOptional.get().getIsDefaultTab() == null || !tabTableOptional.get().getIsDefaultTab())) {
				List<TabTableData> tabTableDataList = tabTableDataRepository.findAllByTabTableId(payload.getTabId());
				List<Long> idList = tabTableDataList.stream().map(TabTableData::getRefId).collect(Collectors.toList());
				payload.setIds(idList);
				payload.setIsDefaultTab(tabTableOptional.get().getIsDefaultTab());
			}
		}
	}

}
