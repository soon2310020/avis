package saleson.api.role.payload;

import lombok.Data;
import saleson.common.enumeration.DashboardGroupType;
import saleson.common.enumeration.GraphType;
import saleson.common.enumeration.RoleType;
import saleson.common.util.StringUtils;
import saleson.model.Menu;
import saleson.model.Role;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class RolePayload {

	private String name;
	private String authority;

	private String description;
	private RoleType roleType;

	private Long[] menuIds;
	private Boolean enabled;

	private DashboardGroupType dashboardGroupType;
	private List<GraphType> graphTypes;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public RoleType getRoleType() {
		return roleType;
	}

	public void setRoleType(RoleType roleType) {
		this.roleType = roleType;
	}

	public Long[] getMenuIds() {
		return menuIds;
	}

	public void setMenuIds(Long[] menuIds) {
		this.menuIds = menuIds;
	}

	public List<GraphType> getGraphTypes(){ return graphTypes; }

	public void setGraphTypes(List<GraphType> graphTypes){ this.graphTypes = graphTypes; }

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Role getModel() {


		Role role = new Role();
		role.setEnabled(true);
		bindData(role);
		return role;
	}

	public Role getModel(Role role) {
		bindData(role);
		return role;
	}



	private void bindData(Role role) {

		role.setName(StringUtils.trimWhitespace(name));
		role.setDescription(StringUtils.trimWhitespace(description));
		role.setAuthority(StringUtils.trimWhitespace(authority));
		role.setRoleType(roleType);
		role.setDashboardGroupType(dashboardGroupType);
		role.setEnabled(role.isEnabled());

		if (menuIds != null) {
			Set<Menu> menuSet = new HashSet<>();

			for (Long id : menuIds) {
				Menu menu = new Menu();
				menu.setId(id);
				menuSet.add(menu);
			}
			role.setMenus(menuSet);
		}
	}
}
