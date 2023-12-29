package com.emoldino.api.common.resource.composite.datimp.service.resoption;

import java.util.Arrays;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.api.common.resource.composite.datimp.enumeration.DatImpResourceType;
import com.emoldino.api.common.resource.composite.datimp.service.DatImpService.ResOption;
import com.emoldino.api.common.resource.composite.datimp.service.DatImpService.ResOptionGetter;
import com.emoldino.api.common.resource.composite.datimp.util.DatImpUtils;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.ValueUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import saleson.api.location.LocationController;
import saleson.api.location.LocationRepository;
import saleson.api.location.LocationService;
import saleson.api.location.payload.LocationPayload;
import saleson.common.enumeration.ObjectType;
import saleson.common.payload.ApiResponse;
import saleson.model.Location;
import saleson.service.googleapi.AddressGeocoderService;

@Getter
@NoArgsConstructor
public class DatImpPlantOption implements ResOptionGetter<LocationPayload> {
	private DatImpResourceType resourceType = DatImpResourceType.PLANT;
	private ResOption<LocationPayload> resOption = new ResOption<LocationPayload>(//
			// 1. Sheet Name
			Arrays.asList("Plant"), //
			// 2. Object Type
			ObjectType.LOCATION, //
			// 3. Item Class
			LocationPayload.class, //
			// 4. Code Field
			Arrays.asList("locationCode"),
			// 5. Populate
			item -> populate(item),
			// 6. Before Logic
			item -> doBefore(item), //
			// 7. Exists Logic
			item -> exists(item), //
			// 8. Post Logic
			item -> post(item), //
			// 9. Put Logic
			item -> put(item)//
	);

	private void populate(LocationPayload item) {
		if (ObjectUtils.isEmpty(item.getLocationCode())) {
			return;
		}
		Location location = BeanUtils.get(LocationRepository.class).findByLocationCode(item.getLocationCode()).orElse(null);
		if (location == null) {
			return;
		}
		ValueUtils.map(location, item);
	}

	private void doBefore(LocationPayload item) {
		item.setCompanyId(DatImpUtils.getCompanyId(item.getCompanyCode(), "companyCode", item.getCompanyId()));
		if (!StringUtils.isEmpty(item.getAddress())) {
			LocationPayload itemLocation = BeanUtils.get(AddressGeocoderService.class).findAddress(item.getAddress());
			if (itemLocation != null) {
				item.setCountryCode(itemLocation.getCountryCode());
				item.setLongitude(itemLocation.getLongitude());
				item.setLatitude(itemLocation.getLatitude());
			}
		}
	}

	private boolean exists(LocationPayload item) {
		return BeanUtils.get(LocationService.class).existsLocationCode(item.getLocationCode(), null);
	}

	private void post(LocationPayload item) {
		item.setEnabled(true);
		ApiResponse response = BeanUtils.get(LocationController.class).newLocation(item);
		if (response.getData() != null && response.getData() instanceof Location) {
			item.setId(((Location) response.getData()).getId());//return id
		}
		DatImpUtils.response(response);
	}

	private void put(LocationPayload item) {
		Location location = BeanUtils.get(LocationRepository.class).findByLocationCode(item.getLocationCode())
				.orElseThrow(() -> DataUtils.newDataNotFoundException(Location.class, DatImpUtils.toXlsColumnTitle("locationCode"), item.getLocationCode()));
		item.setEnabled(location.isEnabled());
		ApiResponse response = BeanUtils.get(LocationController.class).editLocation(item.getId(), item);
		DatImpUtils.response(response);
	}
}
