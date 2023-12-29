package saleson.service.mail;

import org.springframework.context.annotation.Scope;
import saleson.common.infrastructure.mail.*;

@Scope("prototype")
@Template("alertEmailTemplate")
public class AlertEmailService extends EmailContentBuilder {

}
