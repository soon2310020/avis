package saleson.api.location.payload;

import java.util.List;
import java.util.stream.Stream;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;
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
import saleson.model.Location;
import saleson.model.QLocation;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationPayload extends SearchParam {
	private Boolean enabled;
	private String locationCode;
	private String address;
	private String name;
	private String memo;

	private Long companyId;
	private String companyCode;
	private String companyName;

	private String countryCode;
	private String timeZoneId;
	private Double latitude;
	private Double longitude;

	private Long tabId;

	private List<Long> ids;

	private Boolean isModalSelected;
	private Boolean isDefaultTab;

	private MultipartFile[] files;

	private Long dataRequestId;

	public Predicate getPredicate() {
		QLocation location = QLocation.location;
		BooleanBuilder builder = new BooleanBuilder();
//		builder.and(location.company.isEmoldino.isFalse());

		if (AccessControlUtils.isAccessFilterRequired()) {
/*
			builder.and(location.company.id.in(JPQLQueryUtils.getSubCompanyIdSubQuery())//
					.or(location.createdBy.eq(SecurityUtils.getUserId())));
*/
			builder.and(location.company.id.in(AccessControlUtils.getAllAccessibleCompanyIds())//
					);
		}
		if (SecurityUtils.getCompanyType().equals(CompanyType.TOOL_MAKER)) {
			builder.and(location.company.id.eq(SecurityUtils.getCompanyId()));
		}

		if (!StringUtils.isEmpty(getStatus())) {
			Stream.of(CompanyType.values())//
					.filter(companyType -> getStatus().equalsIgnoreCase(companyType.name()))//
					.forEach(companyType -> builder.and(location.company.companyType.eq(companyType)));

			if ("disabled".equalsIgnoreCase(getStatus())) {
				builder.and(location.enabled.isFalse());
			}
		}

		if (!StringUtils.isEmpty(getStatus()) && "disabled".equalsIgnoreCase(getStatus())) {
			builder.and(location.enabled.isFalse());
		} else {
			builder.and(location.enabled.isTrue());
		}

		if (getQuery() != null && !getQuery().isEmpty()) {
			if (isModalSelected != null && isModalSelected) {
				builder.and(//
						location.name.contains(getQuery())//
								.or(location.locationCode.contains(getQuery()))//
								.or(location.company.name.contains(getQuery()))//
				);
			} else {
				builder.and(//
						location.name.contains(getQuery())//
								.or(location.locationCode.contains(getQuery()))//
								.or(location.company.name.contains(getQuery()))//
								.or(location.company.companyCode.contains(getQuery()))//
				);
			}

		}

		if (CollectionUtils.isNotEmpty(ids) || (isDefaultTab != null && !isDefaultTab)) {
			builder.and(location.id.in(ids));
		}
		if (name!=null&&!StringUtils.isEmpty(name)){
			builder.and(location.name.contains(name));
		}

		return builder;
	}

	public Location getModel() {
		Location location = new Location();
		bindData(location);
		return location;
	}

	public Location getModel(Location location) {
		bindData(location);
		return location;
	}

	private void bindData(Location location) {
		location.setLocationCode(StringUtils.trimWhitespace(locationCode));
		location.setEnabled(enabled);
		location.setMemo(StringUtils.trimWhitespace(memo));
		location.setName(StringUtils.trimWhitespace(name));
		location.setCompanyId(companyId);
		location.setAddress(StringUtils.trimWhitespace(address));
		location.setCountryCode(countryCode);
		location.setTimeZoneId(timeZoneId);
		location.setLatitude(latitude);
		location.setLongitude(longitude);
	}
}
