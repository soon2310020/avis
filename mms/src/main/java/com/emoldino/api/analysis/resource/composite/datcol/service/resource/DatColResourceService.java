package com.emoldino.api.analysis.resource.composite.datcol.service.resource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColResourceCompany;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColResourceLastUpdateIn;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColResourcePlant;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColResourceSensor;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColResourceTerminal;
import com.emoldino.api.analysis.resource.composite.datcol.enumeration.DatColResourceType;
import com.emoldino.api.common.resource.base.client.util.ClientUtils;
import com.emoldino.api.common.resource.base.client.util.ClientUtils.ClientInfo;
import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.dto.ListIn;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.HttpUtils;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.api.company.CompanyRepository;
import saleson.api.counter.CounterRepository;
import saleson.api.location.LocationRepository;
import saleson.api.terminal.TerminalRepository;
import saleson.model.Company;
import saleson.model.Counter;
import saleson.model.Location;
import saleson.model.Terminal;
import saleson.model.support.Equipment;

@Service
public class DatColResourceService {
	private static final boolean TEST = false;

	private static String getCetralUrl() {
		String url = ConfigUtils.getCentralUrl();
//		url = "http://localhost:8080";
		return url;
	}

	private static final int TIMEOUT = 30000;

	@Transactional(propagation = Propagation.NEVER)
	public void upload() {
		if (!TEST && //
				("central".equals(ConfigUtils.getServerName()) || "emoldino".equals(ConfigUtils.getServerName()))) {
			return;
		}

		String server = ConfigUtils.getServerName();
		if (!ClientUtils.exists(server)) {
			return;
		}

		ClientInfo client = ClientUtils.get(server);
		if (!client.isUploadEnabled()) {
			return;
		}

		JobUtils.runIfNotRunning("DatColResource.upload", new JobOptions().setClustered(true), () -> {
			String memo = server + "(" + client.getName() + ")";

			uploadResources(DatColResourceType.COMPANY, BeanUtils.get(CompanyRepository.class), "companyCode", memo, DatColResourceCompany.class);
			uploadResources(DatColResourceType.PLANT, BeanUtils.get(LocationRepository.class), "locationCode", memo, DatColResourcePlant.class);
			uploadResources(DatColResourceType.TERMINAL, BeanUtils.get(TerminalRepository.class), "equipmentCode", memo, DatColResourceTerminal.class);
			uploadResources(DatColResourceType.SENSOR, BeanUtils.get(CounterRepository.class), "equipmentCode", memo, DatColResourceSensor.class);
		});

	}

//	private static String getCompanyMemo() {
//		StringBuilder buf = new StringBuilder();
//		int[] i = { 0 };
//		BeanUtils.get(CompanyRepository.class).findAll(new BooleanBuilder().and(QCompany.company.companyType.eq(CompanyType.IN_HOUSE)), Sort.by(Direction.ASC, "id"))
//				.forEach(company -> {
//					if (ObjectUtils.isEmpty(company.getName()) || !company.getName().toLowerCase().contains("emoldino")) {
//						String label = ValueUtils.toLabel(false, company.getName(), company.getCompanyCode());
//						buf.append(i[0]++ == 0 ? "" : ", ").append(label);
//					}
//				});
//
//		String memo = buf.toString();
//		return memo;
//	}

	private static <REPO, T, ITEM> void uploadResources(DatColResourceType resType, QuerydslPredicateExecutor<T> repo, String codeFieldName, String memo, Class<ITEM> itemType) {
		String url = getCetralUrl() + "/api/analysis/dat-col/resources/" + resType.name();
		DataUtils.runContentBatch(repo, new BooleanBuilder(), Sort.by("id"), 100, true, content -> {
			Map<String, T> map = new LinkedHashMap<>();
			content.forEach(data -> map.put(ValueUtils.getString(data, codeFieldName), data));

			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.put("id", new ArrayList<>(map.keySet()));
			Map<String, String> updatedAts = HttpUtils.get(//
					url + "/updated-at", //
					null, params, null, //
					null, new ParameterizedTypeReference<Map<String, String>>() {
					}, TIMEOUT);

			ListIn<ITEM> input = new ListIn<>();
			for (String id : updatedAts.keySet()) {
				String updatedAt = updatedAts.get(id);
				T data = map.get(id);
				if (data == null) {
					LogUtils.saveErrorQuietly(ErrorType.LOGIC, "UNMATCHED_CENTRAL_ID", HttpStatus.EXPECTATION_FAILED, "Cannot Match Central Code:" + id);
					continue;
				}
				if (ObjectUtils.isEmpty(updatedAt)
						|| updatedAt.compareTo(DateUtils2.format(((Instant) ValueUtils.get(data, "updatedAt")).plusSeconds(180), DatePattern.yyyyMMddHHmmss, Zone.GMT)) <= 0) {
					ITEM item = ValueUtils.map(data, itemType);
					ValueUtils.set(item, "memo", memo);
					input.add(item);
				}
			}
			if (input.getContent().isEmpty()) {
				return;
			}
			HttpUtils.call(//
					HttpMethod.POST, url, //
					null, null, input, null, //
					SuccessOut.class, null, //
					TIMEOUT);
		});
	}

	public Map<String, String> getUpdatedAt(DatColResourceType resourceType, DatColResourceLastUpdateIn input) {
		if (!TEST && !"central".equals(ConfigUtils.getServerName())) {
			return Collections.emptyMap();
		} else if (resourceType == null || ObjectUtils.isEmpty(input.getId())) {
			return Collections.emptyMap();
		}

		Map<String, String> output = new LinkedHashMap<>();
		for (String id : input.getId()) {
			try {
				if (DatColResourceType.COMPANY.equals(resourceType)) {
					Company data = BeanUtils.get(CompanyRepository.class).findByCompanyCode(id).orElse(null);
					if (data == null) {
						output.put(id, "20000101000000");
						continue;
					}
					output.put(data.getCompanyCode(), DateUtils2.format(data.getUpdatedAt(), DatePattern.yyyyMMddHHmmss, Zone.GMT));
				} else if (DatColResourceType.PLANT.equals(resourceType)) {
					Location data = BeanUtils.get(LocationRepository.class).findByLocationCode(id).orElse(null);
					if (data == null) {
						output.put(id, "20000101000000");
						continue;
					}
					output.put(data.getLocationCode(), DateUtils2.format(data.getUpdatedAt(), DatePattern.yyyyMMddHHmmss, Zone.GMT));
				} else if (DatColResourceType.TERMINAL.equals(resourceType)) {
					Terminal data = BeanUtils.get(TerminalRepository.class).findByEquipmentCode(id).orElse(null);
					if (data == null) {
						output.put(id, "20000101000000");
						continue;
					}
					output.put(data.getEquipmentCode(), DateUtils2.format(data.getUpdatedAt(), DatePattern.yyyyMMddHHmmss, Zone.GMT));
				} else if (DatColResourceType.SENSOR.equals(resourceType)) {
					Counter data = BeanUtils.get(CounterRepository.class).findByEquipmentCode(id).orElse(null);
					if (data == null) {
						output.put(id, "20000101000000");
						continue;
					}
					output.put(data.getEquipmentCode(), DateUtils2.format(data.getUpdatedAt(), DatePattern.yyyyMMddHHmmss, Zone.GMT));
				}
			} catch (Exception e) {
				// DO Nothing
			}
		}
		return output;
	}

	public void postCompanies(ListIn<DatColResourceCompany> input) {
		if (!TEST && !"central".equals(ConfigUtils.getServerName())) {
			return;
		} else if (ObjectUtils.isEmpty(input.getContent())) {
			return;
		}

		for (DatColResourceCompany item : input.getContent()) {
			TranUtils.doNewTranQuietly(() -> {
				Company data;
				Company oldData = BeanUtils.get(CompanyRepository.class).findByCompanyCode(item.getCompanyCode()).orElse(null);
				if (oldData == null) {
					data = new Company();
				} else {
					data = oldData;
				}
				ValueUtils.map(item, data);
				BeanUtils.get(CompanyRepository.class).save(data);
			});
		}

	}

	public void postPlants(ListIn<DatColResourcePlant> input) {
		if (!TEST && !"central".equals(ConfigUtils.getServerName())) {
			return;
		} else if (ObjectUtils.isEmpty(input.getContent())) {
			return;
		}

		for (DatColResourcePlant item : input.getContent()) {
			TranUtils.doNewTranQuietly(() -> {
				Location data;
				Location oldData = BeanUtils.get(LocationRepository.class).findByLocationCode(item.getLocationCode()).orElse(null);
				if (oldData == null) {
					data = new Location();
				} else {
					data = oldData;
				}
				ValueUtils.map(item, data);
				Company company = null;
				if (!ObjectUtils.isEmpty(item.getCompanyCode())) {
					company = BeanUtils.get(CompanyRepository.class).findByCompanyCode(item.getCompanyCode()).orElse(null);
				}
				data.setCompany(company);
				BeanUtils.get(LocationRepository.class).save(data);
			});
		}

	}

	public void postTerminals(ListIn<DatColResourceTerminal> input) {
		if (!TEST && !"central".equals(ConfigUtils.getServerName())) {
			return;
		} else if (ObjectUtils.isEmpty(input.getContent())) {
			return;
		}

		for (DatColResourceTerminal item : input.getContent()) {
			TranUtils.doNewTranQuietly(() -> {
				Terminal data;
				Terminal oldData = BeanUtils.get(TerminalRepository.class).findByEquipmentCode(item.getEquipmentCode()).orElse(null);
				if (oldData == null) {
					data = new Terminal();
				} else {
					data = oldData;
				}
				ValueUtils.map(item, data);
				populate(data, item.getCompanyCode(), item.getLocationCode());
				BeanUtils.get(TerminalRepository.class).save(data);
			});
		}

	}

	public void postSensors(ListIn<DatColResourceSensor> input) {
		if (!TEST && !"central".equals(ConfigUtils.getServerName())) {
			return;
		} else if (ObjectUtils.isEmpty(input.getContent())) {
			return;
		}

		for (DatColResourceSensor item : input.getContent()) {
			TranUtils.doNewTranQuietly(() -> {
				Counter data;
				Counter oldData = BeanUtils.get(CounterRepository.class).findByEquipmentCode(item.getEquipmentCode()).orElse(null);
				if (oldData == null) {
					data = new Counter();
				} else {
					data = oldData;
				}
				ValueUtils.map(item, data);
				populate(data, item.getCompanyCode(), item.getLocationCode());
				BeanUtils.get(CounterRepository.class).save(data);
			});
		}
	}

	private static void populate(Equipment data, String companyCode, String locationCode) {
		Long companyId = null;
		if (!ObjectUtils.isEmpty(companyCode)) {
			Company company = BeanUtils.get(CompanyRepository.class).findByCompanyCode(companyCode).orElse(null);
			if (company != null) {
				companyId = company.getId();
			}
		}
		data.setCompanyId(companyId);

		Location location = null;
		if (!ObjectUtils.isEmpty(locationCode)) {
			location = BeanUtils.get(LocationRepository.class).findByLocationCode(locationCode).orElse(null);
		}
		data.setLocation(location);
	}

	public void report() {
		if (!TEST && !"central".equals(ConfigUtils.getServerName())) {
			return;
		}

		BooleanBuilder filter = new BooleanBuilder().and(Q.terminal.enabled.isTrue());
	}

}
