package saleson.common.interceptor;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContext;

import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.api.common.resource.base.menu.dto.MenuTreeNode;
import com.emoldino.api.common.resource.base.menu.service.MenuService;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;

import saleson.api.user.UserRepository;
import saleson.common.context.SalesonContext;
import saleson.common.util.SecurityUtils;
import saleson.model.User;

@Component
public class MenuHandlerInterceptor implements HandlerInterceptor {

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		ThreadUtils.doScope("MenuHandlerInterceptor", () -> {
			if (!SecurityUtils.isLogin()) {
				return;
			}

			RequestContext requestContext = new RequestContext(request);
			String uri = requestContext.getRequestUri();
			if (uri.contains("swagger")) {
				return;
			}

			MenuTreeNode menu = BeanUtils.get(MenuService.class).getByUri(uri);
			if (//
			(menu != null && menu.isUnpermitted())//
					// TODO Remove This Condition
					|| (menu == null && BeanUtils.get(MenuService.class).isMenu(uri))) {
				User user = BeanUtils.get(UserRepository.class).getOne(SecurityUtils.getUserId());
				StringBuilder buf = new StringBuilder("Unauthorized Access!! uri:").append(uri).append(", user:");
				if (user == null) {
					buf.append(SecurityUtils.getUserId());
				} else {
					buf.append(user.getName()).append(" (").append(user.getLoginId()).append("-").append(user.getId()).append(")");
				}
				LogUtils.saveErrorQuietly(ErrorType.REQ, "ACCESS_DENIED", HttpStatus.UNAUTHORIZED, buf.toString());
				throw new BizException("ACCESS_DENIED", buf.toString());
			}

			SalesonContext salesonContext = new SalesonContext();
			salesonContext.setUser(BeanUtils.get(UserRepository.class).getOne(SecurityUtils.getUserId()));
			salesonContext.setSsoLogin(SecurityUtils.isSsoLogin());
			ListOut<MenuTreeNode> menuTree = BeanUtils.get(MenuService.class).getTree();

			MenuTreeNode currentMenu = getCurrentFunctionMenu(null, menuTree.getContent());

			salesonContext.setMenuTree(menuTree);
			salesonContext.setCurrentCategoryMenu(getCurrentCategoryMenu(menuTree.getContent()));
			salesonContext.setCurrentFunctionMenu(currentMenu);

			if (modelAndView != null) {
				modelAndView.addObject("requestContext", requestContext);
				modelAndView.addObject("salesonContext", salesonContext);
			}
		});

	}

	private MenuTreeNode getCurrentCategoryMenu(List<MenuTreeNode> content) {
		if (ObjectUtils.isEmpty(content)) {
			return null;
		}
		MenuTreeNode node = content.stream()//
				.filter(t -> ValueUtils.toBoolean(t.getActive(), false))//
				.findFirst()//
				.orElse(content.get(0));
		return node == null || "FUNCTION".equals(node.getType()) ? null : node;
	}

	private MenuTreeNode getCurrentFunctionMenu(MenuTreeNode parent, List<MenuTreeNode> content) {
		if (ObjectUtils.isEmpty(content)) {
			return null;
		}
		MenuTreeNode node = content.stream()//
				.filter(t -> ValueUtils.toBoolean(t.getActive(), false))//
				.findFirst()//
				.orElse(content.get(0));
		if (node == null) {
			return null;
		}

		StringBuilder buf = new StringBuilder();
		if (parent == null) {
			buf.append(node.getName());
		} else {
			buf.append(parent.getPath());
			if (!parent.getName().equalsIgnoreCase(node.getName())) {
				buf.append(" / ").append(parent != null && "CATEGORY".equals(node.getType()) ? node.getName().toUpperCase() : node.getName());
			}
		}
		node.setPath(buf.toString());

		if ("FUNCTION".equals(node.getType())) {
			return node;
		}

		return getCurrentFunctionMenu(node, node.getChildren());
	}
}
