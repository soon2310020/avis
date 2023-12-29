package saleson.service.mail;

import org.springframework.context.annotation.Scope;
import saleson.common.infrastructure.mail.EmailContentBuilder;
import saleson.common.infrastructure.mail.Template;

@Scope("prototype")
@Template("inviteUserEmailTemplate")
public class InviteUserEmailContent extends EmailContentBuilder {

}
