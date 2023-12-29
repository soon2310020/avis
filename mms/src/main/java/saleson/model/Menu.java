package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.hibernate.annotations.Where;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.DateAudit;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Menu extends DateAudit {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "PARENT_ID", insertable = false, updatable = false)
	private Long parentId;
	private Integer level;
	private String menuName;
	private String menuKey;
	private String menuUrl;
	private String icon;

	// 관리자(ROLE_ADMIN)만 접근 가능한 메뉴.
	@Column(length = 1, nullable = false)
	@Convert(converter = BooleanYnConverter.class)
	private boolean adminMenu;

	@Column(length = 1, nullable = false)
	@Convert(converter = BooleanYnConverter.class)
	private boolean enabled;

	@JsonIgnore
	@Where(clause = "ENABLED='Y'")
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "PARENT_ID", insertable = false, updatable = false)
	private Menu parent;

	@JsonIgnore
	@Where(clause = "ENABLED='Y'")
	@OrderBy(value = "ID ASC")
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "PARENT_ID", insertable = false, updatable = false)
	private List<Menu> children = new ArrayList<>();

	private Long position;




	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public String getMenuName() {
		return menuName;
	}

	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}

	public String getMenuKey() {
		return menuKey;
	}

	public void setMenuKey(String menuKey) {
		this.menuKey = menuKey;
	}

	public String getMenuUrl() {
		return menuUrl;
	}

	public void setMenuUrl(String menuUrl) {
		this.menuUrl = menuUrl;
	}

	public Menu getParent() {
		return parent;
	}

	public void setParent(Menu parent) {
		this.parent = parent;
	}

	public List<Menu> getChildren() {
		return children;
	}

	public void setChildren(List<Menu> children) {
		this.children = children;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public boolean isAdminMenu() {
		return adminMenu;
	}

	public void setAdminMenu(boolean adminMenu) {
		this.adminMenu = adminMenu;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
