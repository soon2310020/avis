package com.emoldino.api.common.resource.base.location.util;

import java.time.Instant;
import java.time.ZoneId;

import org.springframework.util.ObjectUtils;

import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;

import saleson.api.company.CompanyRepository;
import saleson.api.counter.CounterService;
import saleson.api.location.LocationRepository;
import saleson.api.location.LocationService;
import saleson.api.mold.MoldRepository;
import saleson.api.terminal.TerminalRepository;
import saleson.common.enumeration.CompanyType;
import saleson.model.Company;
import saleson.model.Counter;
import saleson.model.Location;
import saleson.model.Mold;
import saleson.model.Terminal;

public class LocationUtils {
	public static final String LOCATION_CODE_DEFAULT = "INTERNAL_TEST";

	public static Location getDefault() {
		return BeanUtils.get(LocationRepository.class).findByLocationCode(LOCATION_CODE_DEFAULT).orElseGet(() -> {
			Company company = getEmoldino();

			Location location = new Location();
			location.setLocationCode(LOCATION_CODE_DEFAULT);
			location.setName("Internal Test");
			location.setCompany(company);
			location.setCompanyId(company.getId());
			location.setAddress("South Korea");
			location.setEnabled(true);
			location.setCreatedAt(Instant.now());
			location.setUpdatedAt(Instant.now());
			location = BeanUtils.get(LocationRepository.class).save(location);
			return location;
		});
	}

	private static Company getEmoldino() {
		String companyCode = "emoldino";
		return BeanUtils.get(CompanyRepository.class).findByCompanyCode(companyCode).orElseGet(() -> {
			Company company = new Company();
			company.setCompanyType(CompanyType.IN_HOUSE);
			company.setCompanyCode(companyCode);
			company.setName("Emoldino");
			company.setAddress("South Korea");
			company.setEnabled(true);
			company.setCreatedAt(Instant.now());
			company.setUpdatedAt(Instant.now());
			company = BeanUtils.get(CompanyRepository.class).save(company);
			return company;
		});
	}

	private static final String PROP_ZONE_ID = "LocationUtils.getZoneIdByLocationId::";

	public static String getZoneIdByLocation(Location location) {
		if (location == null) {
			return Zone.SYS;
		}
		String zoneId = ThreadUtils.getProp(PROP_ZONE_ID + location.getId(), () -> ValueUtils.toString(location.getTimeZoneId(), Zone.SYS));
		try {
			ZoneId.of(zoneId);
		} catch (Exception e) {
			zoneId = Zone.SYS;
		}
		return zoneId;
	}

	public static String getZoneIdByLocationId(Long locationId) {
		if (locationId == null || locationId.equals(0L)) {
			return Zone.SYS;
		}
		String zoneId = ThreadUtils.getProp(PROP_ZONE_ID + locationId, () -> {
			Location location = BeanUtils.get(LocationService.class).findById(locationId);
			return getZoneIdByLocation(location);
		});
		return zoneId;
	}

	public static String getZoneIdByTerminalId(Long terminalId) {
		if (ObjectUtils.isEmpty(terminalId)) {
			return Zone.SYS;
		}
		String zoneId = ThreadUtils.getProp("LocationUtils.getZoneIdByTerminalId::" + terminalId, () -> {
			Terminal terminal = BeanUtils.get(TerminalRepository.class).findById(terminalId).orElse(null);
			return getZoneIdByTerminal(terminal);
		});
		return zoneId;
	}

	public static String getZoneIdByTerminalCode(String terminalCode) {
		if (ObjectUtils.isEmpty(terminalCode)) {
			return Zone.SYS;
		}
		String zoneId = ThreadUtils.getProp("LocationUtils.getZoneIdByTerminalCode::" + terminalCode, () -> {
			Terminal terminal = BeanUtils.get(TerminalRepository.class).findByEquipmentCode(terminalCode).orElse(null);
			return getZoneIdByTerminal(terminal);
		});
		return zoneId;
	}

	private static String getZoneIdByTerminal(Terminal terminal) {
		if (terminal == null) {
			return Zone.SYS;
		}
		String zoneId = getZoneIdByLocation(terminal.getLocation());
		ThreadUtils.setProp("LocationUtils.getZoneIdByTerminalId::" + terminal.getId(), zoneId);
		ThreadUtils.setProp("LocationUtils.getZoneIdByTerminalCode::" + terminal.getEquipmentCode(), zoneId);
		return zoneId;
	}

	public static String getZoneIdByCounterCode(String counterCode) {
		if (ObjectUtils.isEmpty(counterCode)) {
			return Zone.SYS;
		}
		String zoneId = ThreadUtils.getProp("LocationUtils.getZoneIdByCounterCode::" + counterCode, () -> {
			Counter counter = BeanUtils.get(CounterService.class).findByEquipmentCode(counterCode).orElse(null);
			if (counter == null) {
				return Zone.SYS;
			}
			return getZoneIdByLocation(counter.getLocation());
		});
		return zoneId;
	}

	public static String getZoneIdByMold(Mold mold) {
		if (mold == null) {
			return Zone.SYS;
		}
		return getZoneIdByLocation(mold.getLocation());
	}

	public static String getZoneIdByMoldId(Long moldId) {
		if (moldId == null) {
			return Zone.SYS;
		}
		String zoneId = ThreadUtils.getProp("LocationUtils.getZoneIdByMoldId::" + moldId, () -> {
			Mold mold = BeanUtils.get(MoldRepository.class).findById(moldId).orElse(null);
			return getZoneIdByMold(mold);
		});
		return zoneId;
	}

}
