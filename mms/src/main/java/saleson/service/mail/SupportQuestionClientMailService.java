package saleson.service.mail;


import org.springframework.context.annotation.Scope;
import saleson.common.infrastructure.mail.*;

import java.util.List;

@Scope("prototype")
@Template("supportQuestionClientMailTemplate")
public class SupportQuestionClientMailService extends EmailContentBuilder {
}
