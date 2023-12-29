package saleson.api.menu;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import saleson.api.user.UserService;
import saleson.common.util.DataUtils;
import saleson.common.util.SecurityUtils;
import saleson.dto.MenuDTO;
import saleson.model.Menu;
import saleson.model.Role;
import saleson.model.User;

@Deprecated
@Service
public class MenuService {
	@Autowired
	MenuRepository menuRepository;
	@Autowired
	UserService userService;

	public Menu findById(Long id) {
		Optional<Menu> menu = menuRepository.findById(id);
		return menu.get();
	}

	public List<Menu> findMenusByParentId(long parentId) {
		return menuRepository.findMenusByParentId(parentId);
	}

	public void processAccessableMenu(Menu menu, List<Long> menuIds) {
		Iterator<Menu> menu2 = menu.getChildren().iterator();
		while (menu2.hasNext()) {
			Menu m2 = menu2.next();

			if (m2.getChildren().isEmpty()) {
				menu2.remove();
				continue;
			}

			if (m2.isAdminMenu() && !SecurityUtils.isAdmin()) {
				menu2.remove();
				continue;
			}

			Iterator<Menu> menu3 = m2.getChildren().iterator();
			while (menu3.hasNext()) {
				Menu m3 = menu3.next();
				if (!menuIds.contains(m3.getId())) {
					menu3.remove();
					continue;
				}

				if (m3.isAdminMenu() && !SecurityUtils.isAdmin()) {
					menu3.remove();
					continue;
				}
			}

			if (m2.getChildren().isEmpty()) {
				menu2.remove();
				continue;
			}
		}
	}

	public List<MenuDTO> getMenuOfUser(Long parentId) {
		List<MenuDTO> resList = new ArrayList<>();

		User user = userService.findById(SecurityUtils.getUserId());
		if (user != null && user.getRoles() != null) {
			Set<Role> roleSet = user.getRoles();
			//by menu
			List<Long> menuIds = new ArrayList<>();
			if (roleSet != null)
				menuIds.addAll(roleSet.stream().flatMap(role -> role.getMenus().stream()).map(m -> m.getId()).distinct().collect(Collectors.toList()));

			List<Menu> allMenus = findMenusByParentId(parentId);
			//sort main menus
			allMenus = allMenus.stream().sorted((o1, o2) -> o1.getPosition() != null ? o1.getPosition().compareTo(o2.getPosition()) : 0).collect(Collectors.toList());
			if (!SecurityUtils.isAdmin() && !allMenus.isEmpty() && allMenus.get(0).getLevel() != null && allMenus.get(0).getLevel().intValue() == 3) {
				allMenus = allMenus.stream().filter(m -> menuIds.contains(m.getId())).collect(Collectors.toList());
			}
			for (Menu m : allMenus) {
				if (!SecurityUtils.isAdmin() && 3000L != m.getId() && 6000L != m.getId()) { // admin은 모든 메뉴 접근 가능.

					// 3. 기본 메뉴에서 menu id 가 일치하지 않는 메뉴는 remove()
					// 		-> admin page && ROLE_ADMIN 이면 권한체크를 하지 않음.
					processAccessableMenu(m, menuIds);
				}
				//sort children menus
				if (m.getChildren() != null) {
					List<Menu> subMenu = m.getChildren().stream().sorted((o1, o2) -> o1.getPosition() != null ? o1.getPosition().compareTo(o2.getPosition()) : 0)
							.collect(Collectors.toList());
					subMenu.forEach(aSubMenu -> {
						List<Menu> subSubMenu = aSubMenu.getChildren().stream().sorted((o1, o2) -> o1.getPosition() != null ? o1.getPosition().compareTo(o2.getPosition()) : 0)
								.collect(Collectors.toList());
						aSubMenu.setChildren(subSubMenu);
					});
					m.setChildren(subMenu);
				}
			}
			resList = allMenus.stream().map(m -> DataUtils.mapper.map(m, MenuDTO.class)).collect(Collectors.toList());
			;
		}

		return resList;
	}
}
