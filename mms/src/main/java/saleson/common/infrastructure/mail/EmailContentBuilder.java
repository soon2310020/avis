package saleson.common.infrastructure.mail;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Objects;


public abstract class EmailContentBuilder {
    protected static final Logger MAIL_LOGGER = LoggerFactory.getLogger(EmailContentBuilder.class);

    private String BASE_DIR = "mail/template/";
    private String mailContent;
    private String templatePath;
//    private Email mail;
    private Object[] htmlVars;

    /**
     * Get Email template from decleration by using annotation.<br>
     * Example:<br>
     * when declare @Template("registration.html") in service file<br>
     *
     * <b>"registration.html"</b> must be a real file name to this parseTemplatePath() can be parse<br>
     */
    private void parseTemplatePath(Locale locale) {
        MAIL_LOGGER.info("\n=================Parsing template =================");
        // TODO: check null
        Template emailTemplate = this.getClass().getAnnotation(Template.class);
        if (Objects.isNull(emailTemplate)) {
//            throw new EmailException(toInt("mail.template_NOT_DEFINED.code"), toStr("mail.template_NOT_DEFINED.msg"));
        }
        templatePath = emailTemplate.value()+checkLocale(locale)+".html";
        // TODO: check valid input
    }


    protected void parseMessage() {
        try (InputStream is = new ClassPathResource(BASE_DIR.concat(templatePath)).getInputStream()) {
            MAIL_LOGGER.info("\n=================Parsing Message=================");
            String templateString = IOUtils.toString(is, Charset.forName("UTF-8"));
            this.mailContent = String.format(templateString
                            .replaceAll("%;","%%;")
                            .replaceAll("%\"","%%\"")
                            .replaceAll("% ","%% ")
                            .replaceAll("%\\)","%%)")
                            .replaceAll("%,","%%,")
                    , this.htmlVars)
                    .replaceAll("%%;", "%;")
                    .replaceAll("%%\"","%\"")
                    .replaceAll("%% ", "% ")
                    .replaceAll("%%\\)",  "%)")
                    .replaceAll("%%,",  "%,")
            ;
        } catch (IOException e) {
            MAIL_LOGGER.info("\n=================An error occur when parsing message =================");
            e.printStackTrace();

//            throw new EmailException(toInt("mail.template.code"), toStr("mail.template.msg"), e);
        }
    }


    public String checkLocale(Locale currentLocale){
        if(currentLocale == null) return "";

        if(currentLocale.equals(Locale.KOREA)){
            return "Korean";
        }
        else if(currentLocale.equals(new Locale("vi", "VN"))) {
            return "Vietnamese";
        }
        else {
            return "English";
        }
    }

    public String getMailContent(){
        this.parseTemplatePath(null);
        this.parseMessage();
        return mailContent;
    }

    public String generateMailContent(Object[] vars) {
        this.htmlVars=vars;
        return getMailContent();
    }

}
