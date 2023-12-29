package saleson.api.machine.payload;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.domain.SearchParam;
import saleson.common.util.DataUtils;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.model.Company;
import saleson.model.Location;
import saleson.model.Machine;
import saleson.model.QMachine;
import saleson.model.QMold;
import saleson.model.User;
import saleson.model.customField.QCustomFieldValue;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MachinePayload extends SearchParam {

	private String machineCode;

	private boolean enabled;

/*
	private Machine machine;
*/

	private Long companyId;
	private String companyCode;
	private String companyName;

	private Long locationId;
	private String locationCode;
	private String locationName;

	private String line;

	private List<User> engineers = new ArrayList<>();
	private String engineerEmails;

	private String machineMaker;

	private String machineType;

	private String machineModel;

	private Integer machineTonnage;

	private String searchType;

	private List<Long> engineerIds;
	//upload
	private MultipartFile[] pictureFiles;

	private List<Long> ids;

	private List<Long> locationIds;

	private Boolean matchedWithTooling;

	private Long dataRequestId;

	private Long tabId;

	private Boolean isDefaultTab;

	public Predicate getPredicate() {
		BooleanBuilder builder = new BooleanBuilder();

		QMachine machine = QMachine.machine;
		QMold mold = QMold.mold;

		if (CollectionUtils.isNotEmpty(ids) || (isDefaultTab != null && !isDefaultTab)) {
			builder.and(machine.id.in(ids));
		}

		if (locationId != null) {
			builder.and(machine.locationId.eq(locationId));
		}
		if (companyId != null) {
			builder.and(machine.companyId.eq(companyId));
		}

		if (CollectionUtils.isNotEmpty(locationIds)) {
			builder.and(machine.locationId.in(locationIds));
		}

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(machine.company.id.in(AccessControlUtils.getAllAccessibleCompanyIds()));
		}

		if (matchedWithTooling != null) {
			if ( matchedWithTooling) {
				builder.and(machine.id.in(JPAExpressions.select(mold.machineId).from(mold).where(mold.machineId.isNotNull())));
			} else {
				builder.and(machine.id.notIn(JPAExpressions.select(mold.machineId).from(mold).where(mold.machineId.isNotNull())));
			}
		}


		if (!StringUtils.isEmpty(getStatus())) {
			if ("disabled".equalsIgnoreCase(getStatus())) {
				builder.and(machine.enabled.isFalse());
			} else if ("enabled".equalsIgnoreCase(getStatus())) {
				builder.and(machine.enabled.isTrue());
			}
		}

		String query = getQuery();
		if (!StringUtils.isEmpty(query)) {
			builder.and(buildQueryFromSelectedFields(query));
		}
		if (!StringUtils.isEmpty(getQueryMobile())) {
			builder.and(machine.machineCode.contains(getQueryMobile()).or(machine.id.in(JPAExpressions.select(mold.machineId).from(mold).where(mold.equipmentCode.contains(getQueryMobile())))));
		}
		checkDeleted(machine, builder);
		return builder;
	}

	public Predicate getPredicateForMatching() {
		BooleanBuilder builder = new BooleanBuilder();

		QMachine machine = QMachine.machine;

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(machine.company.id.in(AccessControlUtils.getAllAccessibleCompanyIds())//
					.or(machine.createdBy.eq(SecurityUtils.getUserId())));
		}

		String query = getQuery();
		if (!StringUtils.isEmpty(query)) {
			builder.and(machine.machineCode.contains(query).or(machine.machineMaker.contains(query)).or(machine.machineType.contains(query)));
		}
		checkDeleted(machine, builder);
		return builder;
	}

	public Predicate getPredicateForUnMatching() {
		BooleanBuilder builder = new BooleanBuilder();

		QMachine machine = QMachine.machine;

		String query = getQuery();
		if (!StringUtils.isEmpty(query)) {
			builder.and(machine.machineCode.contains(query).or(machine.machineMaker.contains(query)).or(machine.machineType.contains(query))
					.or(machine.mold.equipmentCode.contains(query)));
		}
		checkDeleted(machine, builder);
		return builder;
	}

	public Machine getModel() {

/*
		Machine machine = getMachine();
		if (machine == null) {
			machine = new Machine();
		}
*/
		Machine machine = new Machine();

		bindData(machine);
		return machine;
	}

	public Machine getModel(Machine machine) {
		bindData(machine);
		return machine;
	}

	private void bindData(Machine machine) {
		machine.setMachineCode(StringUtils.trimWhitespace(machineCode));
		machine.setEnabled(enabled);
/*
		if (machine.getCompanyId() == null || !machine.getCompanyId().equals(companyId)) {
			Company c = new Company();
			c.setId(companyId);
			machine.setCompanyId(companyId);
			machine.setCompany(c);
		}
*/
		if (machine.getLocationId() == null || !machine.getLocationId().equals(locationId)) {
			Location c = new Location();
			c.setId(locationId);
			machine.setLocationId(locationId);
			machine.setLocation(c);
		}

		// Add by Mickey.park at 2022.03.30
		// Machine In Charge
		if (engineerIds != null && engineerIds.size() > 0) {
			List<User> engineers = new ArrayList<>();

			for (Long id : engineerIds) {
				User engineer = new User();
				engineer.setId(id);
				engineers.add(engineer);
			}
			machine.setEngineers(engineers);
		} else if (engineerIds != null && engineerIds.size() == 0) {
			machine.setEngineers(new ArrayList<>());
		}

		machine.setLine(StringUtils.trimWhitespace(line));
		machine.setMachineMaker(StringUtils.trimWhitespace(machineMaker));
		machine.setMachineType(StringUtils.trimWhitespace(machineType));
		machine.setMachineModel(StringUtils.trimWhitespace(machineModel));
		machine.setMachineTonnage(machineTonnage);
		machine.setDeleted(getDeleted());
	}

	private void checkDeleted(QMachine machine, BooleanBuilder builder) {
		if (getDeleted() == null) {
			builder.and(machine.deleted.isNull().or(machine.deleted.isFalse()));
		} else if (getDeleted()) {
			builder.and(machine.deleted.isTrue());
		}
	}

	private BooleanBuilder buildQueryFromSelectedFields(String query) {
		QMachine machine = QMachine.machine;
		QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;
		BooleanBuilder builder = new BooleanBuilder();
		if (StringUtils.isEmpty(getSearchType())) {
			builder.or(machine.machineCode.contains(query));
		}
//
		if(getSelectedFields()!=null)
		getSelectedFields().forEach(selectedField -> {
			switch (selectedField) {
			case "company":
				builder.or(machine.company.name.contains(query).or(machine.company.companyCode.contains(query)));
				break;
			case "location":
				builder.or(machine.location.name.contains(query).or(machine.location.locationCode.contains(query)));
				break;
			case "line":
				if (StringUtils.isEmpty(getSearchType()))
					builder.or(machine.line.contains(query));
				else
					builder.or(machine.line.eq(query));
				break;
			case "machineMaker":
				builder.or(machine.machineMaker.contains(query));
				break;
			case "machineType":
				builder.or(machine.machineType.contains(query));
				break;
			case "machineModel":
				builder.or(machine.machineModel.contains(query));
				break;
			case "machineCode":
				if (StringUtils.isEmpty(getSearchType()))
					builder.or(machine.machineCode.contains(query));
				else
					builder.or(machine.machineCode.eq(query));
				break;
			default:
				break;
			}
		});

		List<Long> customFieldIds = DataUtils.getNumericElements(getSelectedFields());

		customFieldIds.forEach(id -> {
			builder.or(machine.id.in(JPAExpressions.select(customFieldValue.objectId).from(customFieldValue).where(customFieldValue.value.contains(query))));
		});
		System.out.println("builder---------------" + builder);
//        builder.or((new String(machine.mold.id)).contains( query ));
//        System.out.println("builder after---------------"+builder);
		return builder;
	}

}
