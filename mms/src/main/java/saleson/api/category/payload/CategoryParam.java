package saleson.api.category.payload;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.domain.SearchParam;
import saleson.common.enumeration.ObjectType;
import saleson.common.util.DataUtils;
import saleson.model.QCategory;
import saleson.model.customField.QCustomFieldValue;

import static saleson.common.config.Const.*;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryParam extends SearchParam {
	private Integer level;
	private Boolean enabled;
	private boolean fetchingData;
	private String periodType;
	private String periodValue;
	private Long categoryId;

	//brandId
	private Long productId;

	//productId
	private Long grandChildId;

	public Predicate getPredicateOld() {
		QCategory category = QCategory.category;
		BooleanBuilder builder = new BooleanBuilder();
		QCategory child = new QCategory("child");
		QCategory grandChild = new QCategory("grandChild");
		builder.and(category.id
				.notIn(JPAExpressions.select(category.id).from(category).where(category.id.eq(0L).and(category.children.isEmpty()).and(category.grandchildren.isEmpty()))));

		if (fetchingData != true || getStatus() != null) {
			if (getStatus() == null || getStatus() == "1") {
				builder.and(category.parentId.isNull());
			} else {
				builder.and(category.level.eq(Integer.parseInt(getStatus())));
			}
		}

		if (categoryId != null) {
			builder.and(category.id.eq(categoryId));
		}

		BooleanBuilder childBuilder = new BooleanBuilder();
		BooleanBuilder grandChildBuilder = new BooleanBuilder();
		if (getEnabled() != null && getEnabled()) {
			builder.and(category.enabled.isTrue());
		}

		childBuilder.and(child.level.eq(2));
		grandChildBuilder.and(child.level.eq(3));
		BooleanBuilder childAndGrandChildBuilder = new BooleanBuilder();

		BooleanBuilder categoryBuilder = new BooleanBuilder();
		if (getQuery() != null && !getQuery().isEmpty()) {
			QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;
			BooleanBuilder queryChildBuilder = new BooleanBuilder();
			BooleanBuilder queryGrandChildBuilder = new BooleanBuilder();
			BooleanBuilder queryCategoryBuilder = new BooleanBuilder();
			queryChildBuilder.or(child.name.contains(getQuery()).or(grandChild.name.contains(getQuery())));
			queryGrandChildBuilder.or(child.name.contains(getQuery()));
			queryCategoryBuilder.or(category.name.contains(getQuery()));

			if(getSelectedFields()!=null)
			getSelectedFields().forEach(fieldName -> {
				switch (fieldName) {
				case "description":
					queryChildBuilder.or(child.description.contains(getQuery()).or(grandChild.description.contains(getQuery())));
					queryGrandChildBuilder.or(child.description.contains(getQuery()));
					queryCategoryBuilder.or(category.description.contains(getQuery()));
					break;
				case "division":
					queryChildBuilder.or(child.division.contains(getQuery()).or(grandChild.division.contains(getQuery())));
					queryGrandChildBuilder.or(child.division.contains(getQuery()));
					queryCategoryBuilder.or(category.division.contains(getQuery()));
					break;
				}
			});
			List<Long> customFieldIds = DataUtils.getNumericElements(getSelectedFields());
			customFieldIds.forEach(id -> {
				JPQLQuery<Long> customFieldValueIdQuery = JPAExpressions.select(customFieldValue.objectId).from(customFieldValue)
						.where(customFieldValue.value.contains(getQuery()));

				queryChildBuilder.or(child.id.in(customFieldValueIdQuery)).or(grandChild.id.in(customFieldValueIdQuery));
				queryGrandChildBuilder.or(child.id.in(customFieldValueIdQuery));
				queryCategoryBuilder.or(category.id.in(customFieldValueIdQuery));
			});

			/*if (getEnabled() != null) {
				if (getEnabled() == true) {
					childBuilder.and(child.enabled.isTrue());
				} else {
					childBuilder.and(child.enabled.isFalse());
				}
			}*/

			categoryBuilder.and(queryCategoryBuilder);
			childBuilder.and(queryChildBuilder);
			grandChildBuilder.and(queryGrandChildBuilder);
		}

		if (getEnabled() != null && !getEnabled()) {
			childBuilder.and(child.enabled.isFalse().or(grandChild.enabled.isFalse()));
			grandChildBuilder.and(child.enabled.isFalse());
			categoryBuilder.and(category.enabled.isFalse());
		}
		JPQLQuery<Long> query = JPAExpressions.selectDistinct(child.parentId).from(child).leftJoin(grandChild).on(grandChild.parentId.eq(child.id)).where(childBuilder);

		JPQLQuery<Long> queryGrandChild = JPAExpressions.select(child.grandParentId).from(child).where(grandChildBuilder);

		childAndGrandChildBuilder.or(category.id.in(query).or(category.id.in(queryGrandChild))).or(categoryBuilder);

		if ((getQuery() != null && !getQuery().isEmpty()) || (getEnabled() != null && !getEnabled())) {
			builder.and(childAndGrandChildBuilder);
		}

		return builder;
	}

	public Predicate getPredicate() {
		QCategory category = QCategory.category;
		BooleanBuilder builder = new BooleanBuilder();
		QCategory product = new QCategory("product");
		QCategory brand = new QCategory("brand");

		builder.and(category.id
				.notIn(JPAExpressions.select(category.id).from(category).where(category.id.eq(0L).and(category.children.isEmpty()).and(category.grandchildren.isEmpty()))));

		if (fetchingData != true || getStatus() != null) {
			if (getStatus() == null || getStatus() == "1") {
				builder.and(category.level.eq(CATEGORY_LEVEL));
			} else {
				builder.and(category.level.eq(Integer.parseInt(getStatus())));
			}
		}

		if (categoryId != null) {
			builder.and(category.id.eq(categoryId));
		}

		BooleanBuilder brandBuilder = new BooleanBuilder();
		BooleanBuilder productBuilder = new BooleanBuilder();


		brandBuilder.and(brand.level.eq(BRAND_LEVEL));
		productBuilder.and(product.level.eq(PRODUCT_LEVEL));
		BooleanBuilder brandAndProductBuilder = new BooleanBuilder();

		BooleanBuilder categoryBuilder = new BooleanBuilder();
		if (getQuery() != null && !getQuery().isEmpty()) {
			QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;
			BooleanBuilder queryBrandBuilder = new BooleanBuilder();
			BooleanBuilder queryProductBuilder = new BooleanBuilder();
			BooleanBuilder queryCategoryBuilder = new BooleanBuilder();
			queryBrandBuilder.or(brand.name.contains(getQuery()));
			queryProductBuilder.or(product.name.contains(getQuery()));
			queryCategoryBuilder.or(category.name.contains(getQuery()));

			if(getSelectedFields()!=null)
			getSelectedFields().forEach(fieldName -> {
				switch (fieldName) {
				case "description":
					queryBrandBuilder.or(brand.description.contains(getQuery()));
					queryProductBuilder.or(product.description.contains(getQuery()));
					queryCategoryBuilder.or(category.description.contains(getQuery()));
					break;
				case "division":
					queryBrandBuilder.or(brand.division.contains(getQuery()));
					queryProductBuilder.or(product.division.contains(getQuery()));
					queryCategoryBuilder.or(category.division.contains(getQuery()));
					break;
				}
			});
			List<Long> customFieldIds = DataUtils.getNumericElements(getSelectedFields());
			customFieldIds.forEach(id -> {
				JPQLQuery<Long> customFieldValueIdQuery = JPAExpressions.select(customFieldValue.objectId).from(customFieldValue)
						.where(customFieldValue.value.contains(getQuery()).and(customFieldValue.customField.objectType.eq(ObjectType.CATEGORY)));

				queryBrandBuilder.or(brand.id.in(customFieldValueIdQuery));
				queryProductBuilder.or(product.id.in(customFieldValueIdQuery));
				queryCategoryBuilder.or(category.id.in(customFieldValueIdQuery));
			});

			/*if (getEnabled() != null) {
				if (getEnabled() == true) {
					childBuilder.and(child.enabled.isTrue());
				} else {
					childBuilder.and(child.enabled.isFalse());
				}
			}*/

			categoryBuilder.and(queryCategoryBuilder);
			brandBuilder.and(queryBrandBuilder);
			productBuilder.and(queryProductBuilder);
		}

		if (getEnabled() != null && !getEnabled()) {
			brandBuilder.and(brand.enabled.isFalse());
			productBuilder.and(product.enabled.isFalse());
			categoryBuilder.and(category.enabled.isFalse());
		}
		if (getEnabled() != null && getEnabled()) {
			builder.and(category.enabled.isTrue());

			brandBuilder.and(brand.enabled.isTrue());
			productBuilder.and(product.enabled.isTrue());
			categoryBuilder.and(category.id.ne(0l));
		}
		JPQLQuery<Long> queryCategoryIdOfBrand = JPAExpressions.selectDistinct(brand.grandParentId).from(brand).where(brandBuilder);

		JPQLQuery<Long> queryCategoryIdOfProduct = JPAExpressions.select(product.grandParentId).from(product).where(productBuilder);

		brandAndProductBuilder.or(category.id.in(queryCategoryIdOfBrand).or(category.id.in(queryCategoryIdOfProduct))).or(categoryBuilder);

		if ((getQuery() != null && !getQuery().isEmpty()) || (getEnabled() != null)) {
			builder.and(brandAndProductBuilder);
		}

		return builder;
	}

	public Predicate getPredicateProduct() {
		QCategory category = QCategory.category;
		BooleanBuilder builder = new BooleanBuilder();
		if (categoryId != null) {
			builder.and(category.id.eq(categoryId));
		}
		builder.and(category.level.eq(PRODUCT_LEVEL));
		if (getEnabled() != null && getEnabled()) {
			builder.and(category.enabled.isTrue());
		}
		if (getQuery() != null && !getQuery().isEmpty()) {
			builder.and(category.name.contains(getQuery()));
		}
		return builder;
	}
	public Predicate getPredicateBrand() {
		QCategory category = QCategory.category;
		BooleanBuilder builder = new BooleanBuilder();
		if (categoryId != null) {
			builder.and(category.id.eq(categoryId));
		}
		builder.and(category.level.eq(BRAND_LEVEL));
		if (getEnabled() != null && getEnabled()) {
			builder.and(category.enabled.isTrue());
		}
		if (getQuery() != null && !getQuery().isEmpty()) {
			builder.and(category.name.contains(getQuery()));
		}
		return builder;
	}
}
