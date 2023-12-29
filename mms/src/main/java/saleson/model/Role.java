package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.DashboardGroupType;
import saleson.common.enumeration.RoleType;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.UserDateAudit;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.StreamSupport;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role extends UserDateAudit {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(unique = true)
	private String authority;
	private String name;
	private String description;

	@Enumerated(EnumType.STRING)
	private RoleType roleType;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private boolean enabled;

	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "ROLE_MENU",
			joinColumns = @JoinColumn(name = "ROLE_ID"),
			inverseJoinColumns = @JoinColumn(name = "MENU_ID")
	)
	private Set<Menu> menus = new HashSet<>();

	@Enumerated(EnumType.STRING)
	private DashboardGroupType dashboardGroupType;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Set<Menu> getMenus() {
		return menus;
	}

	public void setMenus(Set<Menu> menus) {
		this.menus = menus;
	}


	public String getRoleTypeName() {
		return this.roleType != null ? this.roleType.getTitle() : "";
	}

	public String[] getMenuIds() {

		if (menus == null || menus.isEmpty()) {
			return new String[0];
		}
		String[] menuIds = StreamSupport.stream(menus.spliterator(), false)
				.map(m -> String.valueOf(m.getId()))
				.toArray(String[]::new);
		return menuIds;
	}

}
