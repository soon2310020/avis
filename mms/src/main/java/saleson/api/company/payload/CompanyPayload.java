package saleson.api.company.payload;

import java.util.List;
import java.util.stream.Stream;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.apache.commons.collections4.CollectionUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;
import saleson.common.domain.SearchParam;
import saleson.common.enumeration.CompanyType;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.model.Company;
import saleson.model.QCompany;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyPayload extends SearchParam {
	private Long id;
	private CompanyType companyType;
	private String companyTypeName;
	private String companyCode;
	private String name;
	private String address;
	private String manager;
	private String phone;
	private String email;
	private String memo;
	private boolean enabled;
	private Boolean forReport;

	// 차트 종류
	private String chartType;

	private List<Long> companyIdNotInList;

	private List<Long> ids;

	private Long tabId;

	private Boolean isModalSelected;

	private Boolean isDefaultTab;

	private Long dataRequestId;

	private MultipartFile[] files;

	private boolean ignoreEMoldino = false;

	public Company getModel() {
		Company company = new Company();
		bindData(company);
		return company;
	}

	public Company getModel(Company company) {
		bindData(company);
		return company;
	}

	private void bindData(Company company) {
		company.setAddress(StringUtils.trimWhitespace(address));
		company.setCompanyCode(StringUtils.trimWhitespace(companyCode));
		company.setCompanyType(companyType);
		company.setEnabled(enabled);
		company.setMemo(StringUtils.trimWhitespace(memo));
		company.setName(StringUtils.trimWhitespace(name));
		company.setPhone(StringUtils.trimWhitespace(phone));
		company.setEmail(StringUtils.trimWhitespace(email));
		company.setManager(StringUtils.trimWhitespace(manager));
	}

	public Predicate getPredicate() {
		QCompany company = QCompany.company;
		BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			if ((!SecurityUtils.getCompanyType().equals(CompanyType.TOOL_MAKER)
					&& (StringUtils.isEmpty(getStatus()) || !getStatus().equalsIgnoreCase(CompanyType.TOOL_MAKER.name())))) {
//			if (AccessControlUtils.isAccessFilterRequired()) {
/*
				builder.and(company.id.in(JPQLQueryUtils.getSubCompanyIdSubQuery()).or(company.createdBy.eq(SecurityUtils.getUserId())));
*/
				builder.and(company.id.in(AccessControlUtils.getAllAccessibleCompanyIds()));
//			}
			} else if ((SecurityUtils.getCompanyType().equals(CompanyType.TOOL_MAKER) && !StringUtils.isEmpty(getStatus())
					&& (getStatus().equalsIgnoreCase("active") || getStatus().equalsIgnoreCase("disabled")))) {
//			if (AccessControlUtils.isAccessFilterRequired()){
				builder.and(company.id.eq(SecurityUtils.getCompanyId()));
//			}
			}
		}

		if (!StringUtils.isEmpty(getStatus())) {

			Stream.of(CompanyType.values()).filter(companyType -> getStatus().equalsIgnoreCase(companyType.name())).forEach(companyType -> {
				builder.and(company.companyType.eq(companyType));
			});

			if ("disabled".equalsIgnoreCase(getStatus())) {
				builder.and(company.enabled.isFalse());
			}
		}

		if (!StringUtils.isEmpty(getStatus()) && "disabled".equalsIgnoreCase(getStatus())) {
			builder.and(company.enabled.isFalse());
		} else {
			builder.and(company.enabled.isTrue());
		}

		if (getQuery() != null && !getQuery().isEmpty()) {
			if (isModalSelected != null && isModalSelected) {
				builder.and(
						company.name.contains(getQuery())
								.or(company.companyCode.contains(getQuery())));
			} else {
				builder.and(
						company.name.contains(getQuery())
								.or(company.companyCode.contains(getQuery()))
								.or(company.address.contains(getQuery()))
				);
			}

		}

		if(companyIdNotInList != null && !companyIdNotInList.isEmpty()) {
			builder.and(company.id.notIn(companyIdNotInList));
		}

		if (forReport != null && getForReport()) {
			builder.and(company.companyType.ne(CompanyType.TOOL_MAKER));
			builder.and(company.isEmoldino.isFalse());
		}

		if (CollectionUtils.isNotEmpty(ids) || (isDefaultTab != null && !isDefaultTab)) {
			builder.and(company.id.in(ids));
		}
		if (ignoreEMoldino) {
			builder.and(company.isEmoldino.isFalse().or(company.isEmoldino.isNull()));
		}
		if (name!=null&&!StringUtils.isEmpty(name)){
			builder.and(company.name.contains(name));
		}


		return builder;
	}
}
