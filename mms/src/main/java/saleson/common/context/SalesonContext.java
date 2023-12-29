package saleson.common.context;

import com.emoldino.api.common.resource.base.menu.dto.MenuTreeNode;
import com.emoldino.framework.dto.ListOut;

import lombok.Data;
import saleson.model.Menu;
import saleson.model.User;

@Data
public class SalesonContext {
	private User user;
	private boolean ssoLogin;

	private ListOut<MenuTreeNode> menuTree;
	private MenuTreeNode currentCategoryMenu;
	private MenuTreeNode currentFunctionMenu;

	@Deprecated
	private Menu menu;
	@Deprecated
	private Menu oveview;
	@Deprecated
	private Menu firstAdminMenu;
	@Deprecated
	private Menu firstSupportMenu;
	@Deprecated
	private Menu firstInsightMenu;
	@Deprecated
	private Menu firstReportMenu;
	@Deprecated
	private Menu alertMenu;
	@Deprecated
	private Menu oeeMenu;

}
