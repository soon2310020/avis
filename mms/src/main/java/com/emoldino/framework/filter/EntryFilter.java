package com.emoldino.framework.filter;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.core.ApplicationFilterConfig;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.ReflectionUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;

public class EntryFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		Exception[] es = { null };

		String uri = request.getRequestURI();

		// Excludes Filters for Static Resources
		if ("developer".equals(ConfigUtils.getProfile()) && !ObjectUtils.isEmpty(uri) && !uri.startsWith("/api/") && (//
		uri.endsWith(".html") || uri.endsWith(".css") || uri.endsWith(".map") || uri.endsWith(".vue") || uri.endsWith(".js")//
				|| uri.endsWith(".png") || uri.endsWith(".gif") || uri.endsWith(".svg") || uri.endsWith(".jpg") || uri.endsWith(".ico")//
				|| uri.endsWith(".ttf") || uri.endsWith(".woff") || uri.endsWith(".woff2")//
//				|| uri.equals("/login") //
				|| uri.equals("/common") || uri.equals("/common/") //
				|| uri.equals("/asset") || uri.equals("/asset/") //
				|| uri.equals("/supplychain") || uri.equals("/supplychain/") //
				|| uri.equals("/production") || uri.equals("/production/") //
				|| uri.equals("/analysis") || uri.equals("/analysis/"))) {
			try {
				Field field = ReflectionUtils.getField(filterChain, "filters");
				field.setAccessible(true);
				ApplicationFilterConfig[] configs = (ApplicationFilterConfig[]) field.get(filterChain);
				if (!ObjectUtils.isEmpty(configs)) {
					for (ApplicationFilterConfig config : configs) {
						if (config == null || ObjectUtils.isEmpty(config.getFilterName())) {
							continue;
						}
						String alreadyFilteredAttributeName = config.getFilterName() + ".FILTERED";
						request.setAttribute(alreadyFilteredAttributeName, Boolean.TRUE);
					}
				}
				request.setAttribute("org.springframework.session.web.http.SessionRepositoryFilter.FILTERED", Boolean.TRUE);
				request.setAttribute("org.springframework.security.web.FilterChainProxy.APPLIED", Boolean.TRUE);
				request.setAttribute("org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter.FILTERED", Boolean.TRUE);
				filterChain.doFilter(request, response);
				return;
			} catch (Exception e) {
				es[0] = e;
			}
		} else {
			ThreadUtils.doScope("emoldino.EntryFilter", () -> {
				try {
					filterChain.doFilter(request, response);
				} catch (Exception e) {
					es[0] = e;
				}
			});
		}

		if (es[0] != null) {
			if (es[0] instanceof RuntimeException) {
				throw (RuntimeException) es[0];
			} else if (es[0] instanceof ServletException) {
				throw (ServletException) es[0];
			} else if (es[0] instanceof IOException) {
				throw (IOException) es[0];
			} else {
				throw ValueUtils.toAe(es[0], "UNEXPECTED_EXCEPTION");
			}
		}
	}

}
