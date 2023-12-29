package saleson.api.role.payload;

import org.springframework.util.StringUtils;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.domain.SearchParam;
import saleson.common.enumeration.RoleType;
import saleson.model.QRole;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RoleParam extends SearchParam {
	private RoleType roleType;

	public Predicate getPredicate() {
		QRole role = QRole.role;
		BooleanBuilder builder = new BooleanBuilder();

		if (!StringUtils.isEmpty(getQuery())) {
			builder.and(role.name.like('%' + getQuery() + '%').or(role.name.like('%' + getQuery() + '%')).or(role.description.like('%' + getQuery() + '%'))
					.or(role.authority.like('%' + getQuery() + '%')));
		}

		if ("group".equals(getStatus())) {
			builder.and(role.roleType.eq(RoleType.ROLE_GROUP));

		} else if ("menu".equals(getStatus())) {
			builder.and(role.roleType.eq(RoleType.ROLE_MENU));
		}
		if ("disabled".equalsIgnoreCase(getStatus())) {
			builder.and(role.enabled.isFalse());
		} else {
			builder.and(role.enabled.isTrue());
		}

		return builder;
	}
}
