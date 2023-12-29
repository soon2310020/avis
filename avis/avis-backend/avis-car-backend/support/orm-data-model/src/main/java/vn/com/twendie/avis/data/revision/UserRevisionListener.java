package vn.com.twendie.avis.data.revision;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.com.twendie.avis.data.enumtype.ActionTypeEnum;
import vn.com.twendie.avis.data.model.User;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.Objects;

@Slf4j
public class UserRevisionListener implements RevisionListener {
    @Override
    public void newRevision(Object revisionEntity) {
        UserRevisionEntity userRevisionEntity = (UserRevisionEntity) revisionEntity;
        userRevisionEntity.setCreatedAt(new Timestamp(userRevisionEntity.getTimestamp()));
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (Objects.nonNull(authentication)) {
                Object principal = authentication.getPrincipal();
                if (Objects.nonNull(principal)) {
                    User user = (User) principal.getClass().getMethod("getUser").invoke(principal);
                    if (Objects.nonNull(user)) {
                        userRevisionEntity.setCreatedBy(user.getId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error when inject user info to revision", e);
        }
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            ActionTypeEnum actionTypeEnum = ActionTypeEnum.getActionTypeFromUri(request.getRequestURI());
            if (Objects.nonNull(actionTypeEnum)) {
                userRevisionEntity.setActionType(actionTypeEnum.getId());
            }
        }
    }
}
