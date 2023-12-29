package saleson.api.accessHierachy.payload;

import java.util.List;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.domain.SearchParam;
import saleson.model.AccessHierarchy;
import saleson.model.QAccessMold;
import saleson.model.QMold;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessHierarchyPayload extends SearchParam {
	private Long companyId;//company id use for create new relation
	private Long companyParentId;//company parent id use for create new relation. null where create root
	private List<Long> parentIdList;//using for update
//	private String parentIdListStr;
	// for access mold
	private Long companyOwnerId;
	// create access mold
	private Long accessCompanyRelationId;
	private List<AccessMoldPayload> accessMoldPayloadList;

	public AccessHierarchy getModel() {
		AccessHierarchy accessHierarchy = new AccessHierarchy();
		bindData(accessHierarchy);
		return accessHierarchy;
	}

	public AccessHierarchy getModel(AccessHierarchy accessHierarchy) {
		bindData(accessHierarchy);
		return accessHierarchy;
	}

	private void bindData(AccessHierarchy accessHierarchy) {
		accessHierarchy.setCompanyId(companyId);
//        accessHierarchy.setCompanyParentId(parentId);
		if (parentIdList != null) {
			accessHierarchy.setParentIdListTemp(parentIdList);
		} else if (companyParentId != null) {
			List<Long> parentIdListUpdate = accessHierarchy.getParentIdList();
			if (!parentIdListUpdate.contains(companyParentId)) {
				parentIdListUpdate.add(companyParentId);
			}
			accessHierarchy.setParentIdListTemp(parentIdListUpdate);
		}
	}

	public Predicate getPredicate() {
//		QAccessHierarchy accessHierarchy = QAccessHierarchy.accessHierarchy;
		BooleanBuilder builder = new BooleanBuilder();
		return builder;
	}

	public Predicate getPredicateAccessMold() {
		QAccessMold accessMold = QAccessMold.accessMold;
		BooleanBuilder builder = new BooleanBuilder();

		if (companyId != null) {
			builder.and(accessMold.companyId.eq(companyId));
		}
		if (companyOwnerId != null) {
			builder.and(accessMold.mold.location.companyId.eq(companyOwnerId));
		}
		if (accessCompanyRelationId != null)
			builder.and(accessMold.accessCompanyRelationId.eq(accessCompanyRelationId));

		return builder;
	}

	public Predicate getPredicateMoldOfCompany() {
		QMold mold = QMold.mold;
		BooleanBuilder builder = new BooleanBuilder();
		if (companyOwnerId != null) {
			builder.and(mold.location.companyId.eq(companyOwnerId));
		}
		builder.and(mold.deleted.isNull().or(mold.deleted.isFalse()));
		return builder;
	}

}
