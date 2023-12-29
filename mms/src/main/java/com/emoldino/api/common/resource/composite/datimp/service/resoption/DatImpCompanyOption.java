package com.emoldino.api.common.resource.composite.datimp.service.resoption;

import java.util.Arrays;

import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.composite.datimp.enumeration.DatImpResourceType;
import com.emoldino.api.common.resource.composite.datimp.service.DatImpService.ResOption;
import com.emoldino.api.common.resource.composite.datimp.service.DatImpService.ResOptionGetter;
import com.emoldino.api.common.resource.composite.datimp.util.DatImpUtils;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.ValueUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import saleson.api.company.CompanyController;
import saleson.api.company.CompanyRepository;
import saleson.api.company.CompanyService;
import saleson.api.company.payload.CompanyPayload;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.ObjectType;
import saleson.common.payload.ApiResponse;
import saleson.model.Company;

@Getter
@NoArgsConstructor
public class DatImpCompanyOption implements ResOptionGetter<CompanyPayload> {
	private DatImpResourceType resourceType = DatImpResourceType.COMPANY;
	private ResOption<CompanyPayload> resOption = new ResOption<CompanyPayload>(//
			// 1. Sheet Name
			Arrays.asList("Company"), //
			// 2. Object Type
			ObjectType.COMPANY, //
			// 3. Item Class
			CompanyPayload.class, //
			// 4. Code Field
			Arrays.asList("companyCode"),
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

	private void populate(CompanyPayload item) {
		if (ObjectUtils.isEmpty(item.getCompanyCode())) {
			return;
		}
		Company company = BeanUtils.get(CompanyRepository.class).findByCompanyCode(item.getCompanyCode()).orElse(null);
		if (company == null) {
			return;
		}
		ValueUtils.map(company, item);
	}

	private void doBefore(CompanyPayload item) {
		// Company Type
		if (!ObjectUtils.isEmpty(item.getCompanyTypeName())) {
			String sentValue = item.getCompanyTypeName();
			if ("공급사".equals(item.getCompanyTypeName())) {
				item.setCompanyTypeName(CompanyType.SUPPLIER.getTitle());
			} else if ("금형제조사".equals(item.getCompanyTypeName())) {
				item.setCompanyTypeName(CompanyType.TOOL_MAKER.getTitle());
			} else if ("OEM".equals(item.getCompanyTypeName())) {
				item.setCompanyTypeName(CompanyType.IN_HOUSE.getTitle());
			}
			CompanyType companyType = Arrays.stream(CompanyType.values())//
					.filter(t -> t.getTitle().equalsIgnoreCase(item.getCompanyTypeName()))//
					.findFirst()//
					.orElseThrow(() -> DataUtils.newDataNotFoundException(Company.class, DatImpUtils.toXlsColumnTitle("companyTypeName"), sentValue));
			item.setCompanyType(companyType);
		}
	}

	private boolean exists(CompanyPayload item) {
		return BeanUtils.get(CompanyService.class).existsCompanyCode(item.getCompanyCode(), null);
	}

	private void post(CompanyPayload item) {
		item.setEnabled(true);
		ApiResponse response = BeanUtils.get(CompanyController.class).newCompany(item);
		if (response.getData() != null && response.getData() instanceof Company) {
			item.setId(((Company) response.getData()).getId());//return id
		}
		DatImpUtils.response(response);
	}

	private void put(CompanyPayload item) {
		Company company = BeanUtils.get(CompanyRepository.class).findByCompanyCode(item.getCompanyCode())//
				.orElseThrow(() -> DataUtils.newDataNotFoundException(Company.class, DatImpUtils.toXlsColumnTitle("companyCode"), item.getCompanyCode()));
		item.setEnabled(company.isEnabled());
		ApiResponse response = BeanUtils.get(CompanyController.class).editCompany(item.getId(), item);
		DatImpUtils.response(response);
	}
}
