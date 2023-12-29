package saleson.api.checklist.payload;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

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
import saleson.common.enumeration.CompanyType;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.model.checklist.CheckListObjectType;
import saleson.model.checklist.Checklist;
import saleson.model.checklist.ChecklistType;
import saleson.model.checklist.QChecklist;
import saleson.model.checklist.QChecklistCompany;
import saleson.model.checklist.QChecklistUser;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChecklistPayload extends SearchParam {
	private Long companyId;

	private List<Long> userIds;

	private List<Long> companyIds;
	private String checklistCode;
	private Boolean enabled;
	private List<String> checklistItemValue;
	private ChecklistType checklistType;

	private CheckListObjectType objectType;

	//search condition
	private CompanyType companyType;

	public Checklist getModel() {
		Checklist checklist = new Checklist();
		bindData(checklist);

		return checklist;
	}

	public void bindData(Checklist checklist) {
		checklist.setChecklistCode(getChecklistCode());
		if (enabled != null) {
			checklist.setEnabled(enabled);
		} else
			checklist.setEnabled(true);
		checklist.setChecklistItems(checklistItemValue);
		checklist.setChecklistType(checklistType != null ? checklistType : ChecklistType.MAINTENANCE);
		checklist.setObjectType(getObjectType());
	}
	/*
	public List<ChecklistItem> bindChecklistItem(Long checklistId){
	    List<ChecklistItem> checklistItems= new ArrayList<>();
	    if(checklistItemValue!=null){
	        checklistItemValue.stream().forEach(v -> {
	            ChecklistItem checklistItem = new ChecklistItem();
	            checklistItem.setChecklistId(checklistId);
	            checklistItem.setValue(v);
	            checklistItems.add(checklistItem);
	        });
	    }
	    return checklistItems;
	}
	*/

	public Predicate getPredicate() {
		QChecklist checklist = QChecklist.checklist;
		QChecklistCompany checklistCompany = QChecklistCompany.checklistCompany;
		QChecklistUser checklistUser = QChecklistUser.checklistUser;
		BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(checklist.id
					.in(JPAExpressions
							.select(checklistCompany.checklistId).distinct().from(checklistCompany).where(checklistCompany.companyId.in(AccessControlUtils.getAllAccessibleCompanyIds())))
					.or(checklist.createdBy.eq(SecurityUtils.getUserId()))
					.or(checklist.id.in(JPAExpressions.select(checklistCompany.checklistId).distinct().from(checklistCompany)
							.where(checklistCompany.companyId.in(SecurityUtils.getCompanyId()))))
					.or(checklist.id
							.in(JPAExpressions.select(checklistUser.checklistId).distinct().from(checklistUser).where(checklistUser.userId.in(SecurityUtils.getUserId())))));
		}
		if (SecurityUtils.getCompanyType().equals(CompanyType.TOOL_MAKER)) {
			builder.and(checklist.id
					.in(JPAExpressions.select(checklistCompany.checklistId).distinct().from(checklistCompany).where(checklistCompany.companyId.in(SecurityUtils.getCompanyId()))));
		}

		if (!StringUtils.isEmpty(getQuery())) {
			builder.and(buildQueryFromSelectedFields(getQuery()));
		}
		if (enabled != null) {
			builder.and(checklist.enabled.eq(enabled));
		}
		if (companyId != null) {
			builder.and(checklist.id.in(JPAExpressions.select(checklistCompany.checklistId).distinct().from(checklistCompany).where(checklistCompany.companyId.in(companyId))));
		}
		if (companyType != null) {
			builder.and(checklist.id
					.in(JPAExpressions.select(checklistCompany.checklistId).distinct().from(checklistCompany).where(checklistCompany.company.companyType.eq(companyType))));
		}
		if (checklistType != null) {
			builder.and(checklist.checklistType.eq(checklistType));
		} else if (!CheckListObjectType.PICK_LIST.equals(objectType)) {
			builder.and(checklist.checklistType.eq(ChecklistType.MAINTENANCE));
		}

		if (objectType == null) {
			builder.and(checklist.objectType.eq(CheckListObjectType.CHECK_LIST));
		} else {
			builder.and(checklist.objectType.eq(objectType));
		}
		return builder;
	}

	private BooleanBuilder buildQueryFromSelectedFields(String query) {
		QChecklist checklist = QChecklist.checklist;
		QChecklistCompany checklistCompany = QChecklistCompany.checklistCompany;
		BooleanBuilder builder = new BooleanBuilder();
		builder.or(checklist.checklistCode.contains(query));

		if (CollectionUtils.isEmpty(getSelectedFields())) {
			builder.or(checklist.id.in(JPAExpressions.select(checklistCompany.checklistId).distinct().from(checklistCompany).where(checklistCompany.company.name.contains(query)))
					.or(checklist.id
							.in(JPAExpressions.select(checklistCompany.checklistId).distinct().from(checklistCompany).where(checklistCompany.company.companyCode.contains(query))))
					.or(checklist.id
							.in(JPAExpressions.select(checklistCompany.checklistId).distinct().from(checklistCompany).where(checklistCompany.company.address.contains(query)))));
		} else {
			getSelectedFields().forEach(selectedField -> {
				switch (selectedField) {
				case "companyName":
					builder.or(checklist.id
							.in(JPAExpressions.select(checklistCompany.checklistId).distinct().from(checklistCompany).where(checklistCompany.company.name.contains(query))));
					break;
				case "companyId":
					builder.or(checklist.id
							.in(JPAExpressions.select(checklistCompany.checklistId).distinct().from(checklistCompany).where(checklistCompany.company.companyCode.contains(query))));
					break;
				case "address":
					builder.or(checklist.id
							.in(JPAExpressions.select(checklistCompany.checklistId).distinct().from(checklistCompany).where(checklistCompany.company.address.contains(query))));
					break;
				default:
					break;
				}
			});
		}

		return builder;
	}

	public CheckListObjectType getObjectType() {
		return objectType != null ? objectType : CheckListObjectType.CHECK_LIST;
	}

	public String getChecklistCode() {
		return checklistCode == null ? null : checklistCode.trim();
	}
}
