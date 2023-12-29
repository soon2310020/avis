package com.emoldino.api.common.resource.composite.datimp.util;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;

import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.Property;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;

import saleson.api.company.CompanyRepository;
import saleson.api.counter.CounterRepository;
import saleson.api.location.LocationRepository;
import saleson.api.part.PartRepository;
import saleson.common.payload.ApiResponse;
import saleson.model.Company;
import saleson.model.Counter;
import saleson.model.Location;
import saleson.model.Part;

public class DatImpUtils {
	public static void response(ApiResponse response) {
		if (response == null || response.isSuccess()) {
			return;
		}
		throw new BizException("message_error", response.getMessage(), new Property("message", response.getMessage()));
	}

	public static void response(ResponseEntity<?> response) {
		if (response == null || response.getStatusCode() == null //
				|| (response.getStatusCodeValue() >= 200 && response.getStatusCodeValue() < 300)) {
			return;
		}
		String message = ValueUtils.abbreviate(ValueUtils.toString(response.getBody(), "Fail!!"), 200);
		throw new BizException("message_error", message, new Property("message", message));
	}

	public static Long getCompanyId(String code, String codeFieldName, Long id) {
		return getCompanyId(null, null, code, codeFieldName, id);
	}

	public static Long getCompanyId(String name, String nameFieldName, String code, String codeFieldName, Long id) {
		if (!ObjectUtils.isEmpty(code)) {
			Company data = BeanUtils.get(CompanyRepository.class).findByCompanyCode(code)
					.orElseThrow(() -> DataUtils.newDataNotFoundException(Company.class, ValueUtils.toString(toXlsColumnTitle(codeFieldName), "company_code"), code));
			return data.getId();
		} else if (!ObjectUtils.isEmpty(name)) {
			Company data = BeanUtils.get(CompanyRepository.class).findByName(name).stream().findFirst()//
					.orElseThrow(() -> DataUtils.newDataNotFoundException(Company.class, ValueUtils.toString(toXlsColumnTitle(nameFieldName), "company_name"), name));
			return data.getId();
		} else {
			return id;
		}
	}

	public static Long getLocationId(String code, String codeFieldName, Long id) {
		return getLocationId(null, null, code, codeFieldName, id);
	}

	public static Long getLocationId(String name, String nameFieldName, String code, String codeFieldName, Long id) {
		if (!ObjectUtils.isEmpty(code)) {
			Location data = BeanUtils.get(LocationRepository.class).findFirstByLocationCode(code)//
					.orElseThrow(() -> DataUtils.newDataNotFoundException(Location.class, ValueUtils.toString(toXlsColumnTitle(codeFieldName), "location_code"), code));
			return data.getId();
		} else if (!ObjectUtils.isEmpty(name)) {
			Location data = BeanUtils.get(LocationRepository.class).findFirstByName(name)//
					.orElseThrow(() -> DataUtils.newDataNotFoundException(Location.class, ValueUtils.toString(toXlsColumnTitle(nameFieldName), "location_name"), name));
			return data.getId();
		} else {
			return id;
		}
	}

	public static Part getPart(String name, String nameFieldName, String code, String codeFieldName, Long id, String idFieldName) {
		if (!ObjectUtils.isEmpty(name)) {
			Part data = BeanUtils.get(PartRepository.class).findByName(name)//
					.orElseThrow(() -> DataUtils.newDataNotFoundException(Part.class, ValueUtils.toString(toXlsColumnTitle(nameFieldName), "part_name"), name));
			return data;
		} else if (!ObjectUtils.isEmpty(code)) {
			Part data = BeanUtils.get(PartRepository.class).findByPartCode(code)//
					.orElseThrow(() -> DataUtils.newDataNotFoundException(Part.class, ValueUtils.toString(toXlsColumnTitle(codeFieldName), "part_code"), code));
			return data;
		} else if (id != null) {
			Part data = BeanUtils.get(PartRepository.class).findById(id)//
					.orElseThrow(() -> DataUtils.newDataNotFoundException(Part.class, ValueUtils.toString(toXlsColumnTitle(idFieldName), "part_id"), name));
			return data;
		} else {
			return null;
		}
	}

	public static Long getSensorId(String code, String codeFieldName, Long id) {
		if (!ObjectUtils.isEmpty(code)) {
			Counter data = BeanUtils.get(CounterRepository.class).findByEquipmentCode(code)//
					.orElseThrow(() -> DataUtils.newDataNotFoundException(Counter.class, ValueUtils.toString(toXlsColumnTitle(codeFieldName), "sensor_code"), code));
			return data.getId();
		} else {
			return id;
		}
	}

	public static String toXlsColumnTitle(String fieldName) {
		@SuppressWarnings("unchecked")
		Map<String, String> map = (Map<String, String>) ThreadUtils.getProp("DatImpService.xlsTitleByFieldNameMap");
		return map == null || !map.containsKey(fieldName) ? fieldName : map.get(fieldName);
	}
}
