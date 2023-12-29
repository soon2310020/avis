package saleson.api.counter.payload;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import saleson.api.equipment.payload.EquipmentPayload;
import saleson.common.enumeration.*;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.model.*;

import java.util.*;
import java.util.stream.Stream;

import static saleson.api.mold.payload.MoldPayload.*;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CounterPayload extends EquipmentPayload {
	private Long moldId;

	private Integer presetCount;

	private Counter counter;
	private MultipartFile[] files;

	private Boolean notMold;
	private String extraStatus;

	private Boolean enabled;

	private String pageSearch;

	private List<MoldStatus> moldStatusList;
	private List<CounterStatus> counterStatusList;

	private List<Long> ids;

	private Long tabId;

	private Boolean isDefaultTab;

	private Integer subscriptionTerm;

	public Integer getPresetCount() {
		return presetCount;
	}

	public void setPresetCount(Integer presetCount) {
		this.presetCount = presetCount;
	}

	public Long getMoldId() {
		return moldId;
	}

	public void setMoldId(Long moldId) {
		this.moldId = moldId;
	}

	public Counter getCounter() {
		return counter;
	}

	public void setCounter(Counter counter) {
		this.counter = counter;
	}

	public MultipartFile[] getFiles() {
		return files;
	}

	public void setFiles(MultipartFile[] files) {
		this.files = files;
	}

	public Boolean getNotMold() {
		return notMold;
	}

	public void setNotMold(Boolean notMold) {
		this.notMold = notMold;
	}

	public String getExtraStatus() {
		return extraStatus;
	}

	public void setExtraStatus(String extraStatus) {
		this.extraStatus = extraStatus;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String getPageSearch() {
		return pageSearch;
	}

	public List<MoldStatus> getMoldStatusList() {
		return moldStatusList;
	}

	public void setMoldStatusList(List<MoldStatus> moldStatusList) {
		this.moldStatusList = moldStatusList;
	}

	public List<CounterStatus> getCounterStatusList() {
		return counterStatusList;
	}

	public void setCounterStatusList(List<CounterStatus> counterStatusList) {
		this.counterStatusList = counterStatusList;
	}

	public void setPageSearch(String pageSearch) {
		this.pageSearch = pageSearch;
	}


	public List<Long> getIds() {
		return ids;
	}

	public void setIds(List<Long> ids) {
		this.ids = ids;
	}

	public Long getTabId() {
		return tabId;
	}

	public void setTabId(Long tabId) {
		this.tabId = tabId;
	}

	public Boolean getDefaultTab() {
		return isDefaultTab;
	}

	public void setDefaultTab(Boolean defaultTab) {
		isDefaultTab = defaultTab;
	}

	public Integer getSubscriptionTerm() {
		return subscriptionTerm;
	}

	public void setSubscriptionTerm(Integer subscriptionTerm) {
		this.subscriptionTerm = subscriptionTerm;
	}

	public Counter getModel() {

		Counter counter = getCounter();
		if (counter == null) {
			counter = new Counter();
			counter.setEnabled(true);
		}

		bindData(counter);
		return counter;
	}

	public Counter getModel(Counter counter) {
		bindData(counter);
		return counter;
	}

	private void bindData(Counter counter) {
		counter.setEquipmentCode(getEquipmentCode());
		counter.setEquipmentStatus(getEquipmentStatus());
		counter.setPurchasedAt(getPurchasedAt());
		counter.setInstalledAt(getInstalledAt());
		counter.setInstalledBy(getInstalledBy());
		counter.setMemo(getMemo());
		counter.setPresetCount(getPresetCount());
		counter.setEnabled(counter.isEnabled());
		if(SecurityUtils.isEmoldino() ){
			counter.setSubscriptionTerm(subscriptionTerm);
		}
	}



	public Predicate getPredicate() {
		QCounter counter = QCounter.counter;
		QMold mold = QMold.mold;
		QPart part = QPart.part;
		QMoldPart moldPart = QMoldPart.moldPart;
		QLocation location = QLocation.location;
		QCompany company = QCompany.company;
		BooleanBuilder builder = new BooleanBuilder();

		// Admin이 아닌 경우 권한이 있는 Mold만 조회함.
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(counter.id.in(JPQLQueryUtils.getCounterIdsSubquery()));
		}
		if(getId()!=null){
			builder.and(counter.id.eq(getId()));
		}
		if (getOperatingStatus() != null) {
			builder.and(counter.operatingStatus.eq(getOperatingStatus()));
		}
		if(getOperatingStatuses() != null){
			builder.and(counter.operatingStatus.in(getOperatingStatuses()));
		}

		if (getEquipmentStatus() != null) {
			builder.and(counter.equipmentStatus.eq(getEquipmentStatus()));
		}
		if (getEquipmentStatuses() != null) {
			builder.and(counter.equipmentStatus.in(getEquipmentStatuses()));
		}

		if (PresetStatus.READY.name().equals(getExtraStatus())) {
			builder.and(counter.presetStatus.eq(PresetStatus.READY));
		}

		if ("NEW_TOOLING".equalsIgnoreCase(getPageSearch())) {
			builder.and(counter.equipmentStatus.in(Arrays.asList(EquipmentStatus.AVAILABLE, EquipmentStatus.DISCARDED)));
		}

		if(notMold!=null && notMold==true){
			builder.and(counter.equipmentStatus.eq(EquipmentStatus.AVAILABLE));
/*
			builder.and(counter.id.notIn(JPAExpressions
					.select(mold.counterId)
					.from(mold)
					.where(mold.counterId.isNotNull()))
					.and(mold.deleted.isNull().or(mold.deleted.isFalse())));
*/
		}

		if (CollectionUtils.isNotEmpty(ids) || (isDefaultTab != null && !isDefaultTab) ) {
			builder.and(counter.id.in(ids));
		}

		if (!StringUtils.isEmpty(getQuery())) {
			builder.and(buildQueryFromSelectedFields(getQuery()));
		}

		if(!StringUtils.isEmpty(getWhere())){
			String[] words = StringUtils.delimitedListToStringArray(getWhere().trim(),",");
			if(words.length > 0){
				Map<String, String> range = new HashMap<>();
				String operation = "and";

				for(String word: words){
					word = word.trim();
					if(word.isEmpty()) continue;
					if(word.startsWith("tool")){
						String[] values = word.split("tool", 2);
						if(values != null && values.length == 2 && !values[1].equals("")) {
							builder.and(counter.id.in(
									JPAExpressions
											.select(mold.counterId)
											.from(mold)
											.where(mold.equipmentCode.contains(values[1])
													.and(mold.deleted.isNull().or(mold.deleted.isFalse())))
							));
						}
					}else if(word.startsWith("part")){
						String[] values = word.split("part", 2);
						if(values != null && values.length == 2 && !values[1].equals("")) {
							builder.and(counter.id.in(
									JPAExpressions
											.select(mold.counter.id)
											.from(mold)
											.innerJoin(moldPart).on(moldPart.moldId.eq(mold.id))
											.innerJoin(part).on(part.id.eq(moldPart.partId))
											.where(part.partCode.contains(values[1])
													.and(mold.deleted.isNull().or(mold.deleted.isFalse()))
													.and(part.deleted.isNull().or(part.deleted.isFalse())))
							));
						}

					} else {
						String op = loadRange(range,word);
						if(!StringUtils.isEmpty(op)) operation=op;
					}
				}

				if(!range.isEmpty()){
					if(operation.equals("and")){

						Predicate[] predicateArray = predicatesMoldPartAndByRange(moldPart,range);

						builder.and(counter.id.in(
								JPAExpressions
										.select(mold.counterId)
										.from(mold)
										.innerJoin(moldPart).on(moldPart.moldId.eq(mold.id))
										.groupBy(moldPart.moldId)
										.having(predicateArray)
						));
					}else{

						Predicate predicate = predicateMoldPartOrByRange(moldPart,range);

						if(predicate != null) {
							builder.and(counter.id.in(
									JPAExpressions
											.select(mold.counterId)
											.from(mold)
											.innerJoin(moldPart).on(moldPart.moldId.eq(mold.id))
											.groupBy(moldPart.moldId)
											.having(predicate)
							));
						}
					}
				}
			}
		}

		if (!StringUtils.isEmpty(getStatus())) {
			Stream.of(CompanyType.values())
					.filter(companyType -> getStatus().equalsIgnoreCase(companyType.name()))
					.forEach(companyType -> {
						builder.and(counter.location.company.companyType.eq(companyType));
					});

			if ("disabled".equalsIgnoreCase(getStatus())) {
				builder.and(counter.enabled.isFalse());
			} else {
				builder.and(counter.enabled.isTrue());
			}
		} else {
			builder.and(counter.enabled.isTrue());
		}
		checkDataStatus(counter, builder);
		return builder;

	}

	private void checkDataStatus(QCounter counter , BooleanBuilder builder) {
		BooleanBuilder statusBuilder =  new BooleanBuilder();
		QMold mold = QMold.mold;
		JPQLQuery<Long> matchCounterIdExpressions = JPAExpressions.select(mold.counterId).from(mold).where(mold.counterId.isNotNull());
		if(moldStatusList != null) {

			for (MoldStatus moldStatus : moldStatusList) {
				switch (moldStatus) {
					case IN_PRODUCTION:{
						statusBuilder.or(new BooleanBuilder().and(
								counter.id.in(JPAExpressions.select(mold.counterId).from(mold).where(mold.operatingStatus.eq(OperatingStatus.WORKING).and(mold.counterId.isNotNull())))
								.and(counter.equipmentStatus.ne(EquipmentStatus.DETACHED))));
						break;
					}
					case IDLE:{
						statusBuilder.or(new BooleanBuilder().and(
								counter.id.in(JPAExpressions.select(mold.counterId).from(mold).where(mold.operatingStatus.eq(OperatingStatus.IDLE).and(mold.counterId.isNotNull())))
								.and(counter.equipmentStatus.ne(EquipmentStatus.DETACHED))));
						break;
					}
					case NOT_WORKING: {
						statusBuilder.or(new BooleanBuilder().and(
								counter.id.in(JPAExpressions.select(mold.counterId).from(mold).where(mold.operatingStatus.eq(OperatingStatus.NOT_WORKING).and(mold.counterId.isNotNull())))
								.and(counter.equipmentStatus.ne(EquipmentStatus.DETACHED))));
						break;
					}
					case SENSOR_OFFLINE:{
						statusBuilder.or(counter.equipmentStatus.ne(EquipmentStatus.DETACHED)
										.and(counter.id.in(matchCounterIdExpressions))
								.and(counter.operatingStatus.eq(OperatingStatus.DISCONNECTED)
										.or(counter.id.in(JPAExpressions.select(mold.counterId).from(mold).where(mold.operatingStatus.eq(OperatingStatus.DISCONNECTED))))
								));
						break;
					}
					case SENSOR_DETACHED:{
						statusBuilder.or(counter.equipmentStatus.eq(EquipmentStatus.DETACHED));
						break;
					}
					case NO_SENSOR:{
						statusBuilder.or(counter.id.notIn(JPAExpressions.select(mold.counterId).from(mold).where(mold.counterId.isNotNull())));
						break;
					}
					case ON_STANDBY:{
						statusBuilder.or(counter.operatingStatus.isNull().and(counter.id.in(JPAExpressions.select(mold.counterId).from(mold).where(mold.operatingStatus.isNull()))));
					}
				}
			}

		}

		if (counterStatusList != null) {
			for (CounterStatus counterStatus : counterStatusList) {
				switch (counterStatus) {
					case INSTALLED:{
						statusBuilder.or(counter.equipmentStatus.eq(EquipmentStatus.INSTALLED));
						break;
					}
					case DETACHED:{
						statusBuilder.or(counter.equipmentStatus.eq(EquipmentStatus.DETACHED));
						break;
					}
					case NOT_INSTALLED: {
						statusBuilder.or(counter.id.notIn(JPAExpressions.select(mold.counterId).from(mold).where(mold.counterId.isNotNull())));
						break;
					}
				}
			}
		}
		builder.and(statusBuilder);

	}

	private BooleanBuilder buildQueryFromSelectedFields(String query) {
		QCounter counter = QCounter.counter;
		QMold mold = QMold.mold;
		QLocation location = QLocation.location;
		QCompany company = QCompany.company;
		BooleanBuilder builder = new BooleanBuilder();
		builder.or(counter.equipmentCode.contains(query));

		if (CollectionUtils.isEmpty(getSelectedFields())) {
			builder.or(counter.equipmentCode.contains(getQuery()))
					.or(location.id.in(
									JPAExpressions
											.select(counter.locationId)
											.from(counter)
											.leftJoin(location).on(counter.locationId.eq(location.id))
											.leftJoin(company).on(location.companyId.eq(company.id))
											.where(location.name.contains(getQuery())
													.or(location.locationCode.contains(getQuery()))
													.or(company.name.contains(getQuery()))
													.or(company.companyCode.contains(getQuery())))))
					.or(counter.id.in(
									JPAExpressions
											.select(mold.counterId)
											.from(mold)
											.where(mold.equipmentCode.contains(getQuery())
													.and(mold.deleted.isNull().or(mold.deleted.isFalse())))));
		} else {
			getSelectedFields().forEach(selectedField -> {
				switch (selectedField) {
					case "mold.equipmentCode":
						builder.or(counter.id.in(
								JPAExpressions
										.select(mold.counterId)
										.from(mold)
										.where(mold.equipmentCode.contains(query)
												.and(mold.deleted.isNull().or(mold.deleted.isFalse())))));
						break;
					case "batteryStatus":
						builder.or(counter.batteryStatus.stringValue().containsIgnoreCase(query));
						break;
//					case "operatingStatus":
//						builder.or(counter.operatingStatus.stringValue().containsIgnoreCase(query));
//						break;
//					case "equipmentStatus":
//						builder.or(counter.equipmentStatus.stringValue().containsIgnoreCase(query));
//						break;
//					case "presetStatus":
//						builder.or(counter.presetStatus.stringValue().containsIgnoreCase(query));
//						break;
					case "memo":
						builder.or(counter.memo.contains(query));
						break;
					case "installedBy":
						builder.or(counter.installedBy.contains(query));
						break;
					case "company":
						builder.or(counter.location.id.in(
								JPAExpressions
										.select(counter.locationId)
										.from(counter)
										.leftJoin(location).on(counter.locationId.eq(location.id))
										.leftJoin(company).on(location.companyId.eq(company.id))
										.where(company.name.contains(query)
												.or(company.companyCode.contains(query)))));
						break;
					case "location":
						builder.or(counter.location.id.in(
								JPAExpressions
										.select(counter.locationId)
										.from(counter)
										.leftJoin(location).on(counter.locationId.eq(location.id))
										.leftJoin(company).on(location.companyId.eq(company.id))
										.where(location.name.contains(query)
												.or(location.locationCode.contains(query)))));
						break;
				}
			});
		}

		return builder;
	}

}
