package saleson.api.part.payload;

import static saleson.api.mold.payload.MoldPayload.loadRange;
import static saleson.api.mold.payload.MoldPayload.predicateMoldPartOrByRange;
import static saleson.api.mold.payload.MoldPayload.predicatesMoldPartAndByRange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.web.multipart.MultipartFile;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.domain.SearchParam;
import saleson.common.enumeration.CurrencyType;
import saleson.common.enumeration.PageType;
import saleson.common.enumeration.SizeUnit;
import saleson.common.enumeration.WeightUnit;
import saleson.common.util.DataUtils;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.dto.CustomField.CustomFieldDTO;
import saleson.model.Part;
import saleson.model.QCategory;
import saleson.model.QCounter;
import saleson.model.QMold;
import saleson.model.QMoldPart;
import saleson.model.QPart;
import saleson.model.customField.QCustomFieldValue;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PartPayload extends SearchParam {
	private List<Long> ids;
	private Long id;
	private Long categoryId;
	private String name;
	private String partCode;
	private String memo;
	private boolean enabled;

	private String resinCode;
	private String resinGrade;
	private String designRevision;
	private Integer weeklyDemand;
	private String size;

	private SizeUnit sizeUnit;
	private String weight;

	private WeightUnit weightUnit;
	private String downstreamSite;

	private Integer price;
	private CurrencyType currencyType;

	private String extraStatus;
//	private AccessType accessType;

	private String startDate;
	private String endDate;
	private String timePeriod;

	//filter available part for a tooling
	private Long availableMoldId;

	//import
	private String categoryName;
	private String productName;

	//custom field
	private List<CustomFieldDTO> customFieldDTOList = new ArrayList<>();

	//upload
	private MultipartFile[] partPictureFiles;

	private Long quantityRequired;

	private PageType pageType;

	private Boolean noProjectAssigned;
	private Boolean dashboardRedirected;
	private Boolean tabbedDashboardRedirected;

	private Long tabId;
	private Boolean isModalSelected;
	private Boolean isDefaultTab;

	private Boolean searchByTooling;

	private String sizeUnitTitle;
	private String weightUnitTitle;

	private Long dataRequestId;

	public Part getModel() {
		Part part = new Part();
		bindData(part);
		return part;
	}

	public Part getModel(Part part) {
		bindData(part);
		return part;
	}

	private void bindData(Part part) {
		part.setPartCode(StringUtils.trimWhitespace(partCode));
		part.setEnabled(enabled);
		part.setMemo(StringUtils.trimWhitespace(memo));
		part.setName(StringUtils.trimWhitespace(name));
		part.setCategoryId(categoryId);

		part.setResinCode(StringUtils.trimWhitespace(resinCode));
		part.setResinGrade(StringUtils.trimWhitespace(resinGrade));
		part.setDesignRevision(StringUtils.trimWhitespace(designRevision));
		part.setWeeklyDemand(weeklyDemand);
		part.setSize(size);

		part.setSizeUnit(sizeUnit);
		part.setWeight(weight);

		part.setWeightUnit(weightUnit);
		part.setDownstreamSite(downstreamSite);

		part.setPrice(price);
		part.setCurrencyType(currencyType);
		if (categoryId == null) {
			part.setQuantityRequired(null);
		} else
			part.setQuantityRequired(quantityRequired);
	}

	@JsonIgnore
	public Predicate getPredicate() {
		QPart part = QPart.part;
		QMold mold = QMold.mold;
		QMoldPart moldPart = QMoldPart.moldPart;
		QCounter counter = QCounter.counter;
		QCategory category = QCategory.category;
		BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(part.id.in(JPQLQueryUtils.getPartIdsSubquery())//
//					.or(part.createdBy.eq(SecurityUtils.getUserId()))//
			);
		}

		if(ids != null && ids.size() > 0 || (isDefaultTab != null && !isDefaultTab)){
			builder.and(part.id.in(ids));
		}
		if (getId() != null) {
			builder.and(part.id.eq(getId()));
		}
		if (!StringUtils.isEmpty(getStatus())) {

			if ("active".equalsIgnoreCase(getStatus())) {
				builder.and(part.enabled.isTrue());

			} else if ("disabled".equalsIgnoreCase(getStatus())) {
				builder.and(part.enabled.isFalse());

			}
		}

		checkDeleted(part, builder);

		if (getCategoryId() != null) {
			builder.and(part.categoryId.eq(getCategoryId()).or(
					part.id.in(JPAExpressions.select(part.id).from(part).leftJoin(category).on(part.categoryId.eq(category.id)).where(category.parent.id.eq(getCategoryId())))));
		}

		if (getQuery() != null && !getQuery().isEmpty()) {
			builder.and(buildQueryFromSelectedFields(getQuery(), searchByTooling));
		}

		if (getAvailableMoldId() != null) {
			builder.and(part.id.in(JPAExpressions.select(moldPart.partId).from(moldPart).where(moldPart.moldId.eq(getAvailableMoldId()).and(moldPart.cavity.eq(0)))));
		}

		if (!StringUtils.isEmpty(getWhere())) {
			String[] words = StringUtils.delimitedListToStringArray(getWhere().trim(), ",");
			if (words.length > 0) {
				Map<String, String> range = new HashMap<>();
				String operation = "and";

				for (String word : words) {
					word = word.trim();
					if (word.isEmpty())
						continue;
					if (word.startsWith("tool")) {
						String[] values = word.split("tool", 2);
						if (values != null && values.length == 2 && !values[1].equals("")) {
							builder.and(part.id.in(JPAExpressions.select(moldPart.partId).from(moldPart).innerJoin(mold).on(mold.id.eq(moldPart.moldId))
									.where(mold.equipmentCode.contains(values[1]).and(mold.deleted.isNull().or(mold.deleted.isFalse())))));
						}
					} else if (word.startsWith("counter")) {
						String[] values = word.split("counter", 2);
						if (values != null && values.length == 2 && !values[1].equals("")) {
							builder.and(part.id.in(JPAExpressions.select(moldPart.partId).from(moldPart).innerJoin(mold).on(moldPart.moldId.eq(mold.id)).innerJoin(counter)
									.on(mold.counterId.eq(counter.id)).where(counter.equipmentCode.contains(values[1]).and(mold.deleted.isNull().or(mold.deleted.isFalse())))));
						}

					} else {
						String op = loadRange(range, word);
						if (!StringUtils.isEmpty(op))
							operation = op;
					}
				}

				if (!range.isEmpty()) {
					if (operation.equals("and")) {

						Predicate[] predicateArray = predicatesMoldPartAndByRange(moldPart, range);
						builder.and(part.id.in(JPAExpressions.select(moldPart.partId).from(moldPart).groupBy(moldPart.partId).having(predicateArray)));
					} else {

						Predicate predicate = predicateMoldPartOrByRange(moldPart, range);
						if (predicate != null) {
							builder.and(part.id.in(JPAExpressions.select(moldPart.partId).from(moldPart).groupBy(moldPart.partId).having(predicate)));
						}
					}
				}
			}
		}

		if (getNoProjectAssigned() != null && getNoProjectAssigned()) {
			builder.and(part.categoryId.isNull());
		}
		if (name!=null&&!StringUtils.isEmpty(name)){
			builder.and(part.name.contains(name));
		}

		return builder;
	}

	private void checkDeleted(QPart part, BooleanBuilder builder) {
		if (getDeleted() == null) {
			builder.and(part.deleted.isNull().or(part.deleted.isFalse()));
		} else if (getDeleted()) {
			builder.and(part.deleted.isTrue());
		}
	}

	@JsonIgnore
	private BooleanBuilder buildQueryFromSelectedFields(String query, Boolean searchByTooling) {
		QPart part = QPart.part;
		QMoldPart moldPart = QMoldPart.moldPart;
		QCategory category = QCategory.category;
		QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;
		BooleanBuilder builder = new BooleanBuilder();
		builder.or(part.name.contains(getQuery()).or(part.partCode.contains(getQuery())));
		if (searchByTooling != null && searchByTooling) {
			builder.or(part.id.in(JPAExpressions
					.select(part.id)
					.from(part).leftJoin(moldPart).on(part.id.eq(moldPart.partId))
					.where(moldPart.mold.equipmentCode.contains(query))));
		}
		if (getSelectedFields() != null && (isModalSelected == null || !isModalSelected)) {
			getSelectedFields().forEach(selectedField -> {
				switch (selectedField) {
					case "name":
						builder.or(part.name.contains(query));
						break;
					case "category":
//					builder.or(part.category.parent.name.contains(query));
						builder.or(part.id.in(JPAExpressions
								.select(part.id)
								.from(part)
								.leftJoin(category).on(part.categoryId.eq(category.id))
								.where(category.parent.name.contains(query))));
						break;
					case "projectName":
//					builder.or(part.category.name.contains(query));
						builder.or(part.id.in(JPAExpressions
								.select(part.id)
								.from(part)
								.leftJoin(category).on(part.categoryId.eq(category.id))
								.where(category.name.contains(query))));
						break;
					case "resinCode":
						builder.or(part.resinCode.contains(query));
						break;
					case "resinGrade":
						builder.or(part.resinGrade.contains(query));
						break;
					default:
						break;
				}
			});
			List<Long> customFieldIds = DataUtils.getNumericElements(getSelectedFields());

			customFieldIds.forEach(id -> {
				builder.or(part.id.in(
						JPAExpressions
								.select(customFieldValue.objectId)
								.from(customFieldValue)
								.where(customFieldValue.value.contains(query))));
			});

		}

		return builder;

	}

}
