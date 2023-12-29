package saleson.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class MenuDTO {
	private Long id;
	private Long parentId;
	private Integer level;
	private String menuName;
	private String menuKey;
	private String menuUrl;
	private String icon;
	private boolean adminMenu;
	private boolean enabled;
	private List<MenuDTO> children = new ArrayList<>();
}
