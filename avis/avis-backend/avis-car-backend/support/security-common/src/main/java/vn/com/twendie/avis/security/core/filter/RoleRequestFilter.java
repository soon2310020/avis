package vn.com.twendie.avis.security.core.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.com.twendie.avis.api.rest.model.ApiError;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.locale.config.Translator;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public abstract class RoleRequestFilter extends OncePerRequestFilter {

    protected RoleRequestFilter() {
    }

    protected abstract Set<String> acceptRoles();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.nonNull(authentication) &&
                authentication.getAuthorities().stream().noneMatch(o -> acceptRoles().contains(o.getAuthority()))) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            PrintWriter writer = response.getWriter();
            writer.println(new ObjectMapper().writeValueAsString(
                    ApiResponse.error(ApiError.builder()
                            .code(HttpStatus.FORBIDDEN.value())
                            .message(Translator.toLocale("auth.permission_deny"))
                            .debugMessage("Permission deny")
                            .extraData(Collections.EMPTY_MAP)
                            .build())
            ));
            writer.close();
            return;
        }
        chain.doFilter(request, response);
    }

}
