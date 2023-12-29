package saleson.api.menu;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import saleson.common.config.Const;
import saleson.dto.MenuDTO;
import saleson.model.Menu;

@Deprecated
@RestController
@RequestMapping("/api/menus")
public class MenuController {
	@Autowired
	private MenuService menuService;

	@GetMapping
	public ResponseEntity<Menu> menus(@RequestParam(value = "type", required = false) String type) {

		Menu menu = null;
		if ("admin".equalsIgnoreCase(type)) {
			menu = menuService.findById(2000L);
		} else if ("front".equalsIgnoreCase(type)) {
			menu = menuService.findById(1000L);
		} else {
			menu = menuService.findById(0L);
		}

		return new ResponseEntity<>(menu, HttpStatus.OK);
	}

	@GetMapping("getMenuOfUser")
	public ResponseEntity<List<MenuDTO>> getMenuOfUser() {
		List<MenuDTO> list = menuService.getMenuOfUser(0L);
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@GetMapping("alert-menus-of-user")
	public ResponseEntity<List<MenuDTO>> getAlertMenusOfUser() {
		List<MenuDTO> list = menuService.getMenuOfUser(Const.MENU_ID.ALERT);
		return new ResponseEntity<>(list, HttpStatus.OK);
	}
}
