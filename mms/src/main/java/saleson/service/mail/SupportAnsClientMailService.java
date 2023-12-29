package saleson.service.mail;


import org.springframework.context.annotation.Scope;
import saleson.common.infrastructure.mail.*;

import java.util.List;

@Scope("prototype")
@Template("supportAnsClientMailTemplate")
public class SupportAnsClientMailService extends EmailContentBuilder {

}
