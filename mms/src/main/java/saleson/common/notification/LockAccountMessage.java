package saleson.common.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.User;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LockAccountMessage extends MailMessage {

  private String host;
  private User user;
  private String token;

  @Builder
  public LockAccountMessage(List<String> receivers, String title, String content, String host, User user, String token) {
    super(receivers, title, content);
    this.host = host;
    this.user = user;
    this.token = token;
  }

  @Override
  public String getTitle() {
    return "[eMoldino Support Team] Reset Your Password";
  }
/*

  @Override
  public String getContent() {
    if(host.charAt(4) == 's'){ host = host.replaceFirst("s", ""); }
    return "<!DOCTYPE html>\n" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:v=\"urn:schemas-microsoft-com:vml\">\n" +
            "\n" +
            "<head>\n" +
            "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>\n" +
            "</head>\n" +
            "\n" +
            "<body style=\"background:#f5f5f5\">\n" +
            "<div style=\"max-width: 970px;padding-left: 30px;\">\n" +
            "  <div>\n" +
            "    <div>\n" +
            "      <div>\n" +
            "        <img src=\"https://user-images.githubusercontent.com/69447666/97524542-4a9f6800-19e8-11eb-897a-261949ca1e9a.png\" style=\"padding-top:22px;padding-bottom:12px;height:50px\"/>\n" +
            "      </div>\n" +
            "    </div>\n" +
            "    <div style=\"padding:40px;background:white\">\n" +
            "      <div>\n" +
            "        <p><b>Hi " + this.user.getName() + ",</b> <br> <br> Your account has been locked due to multiple failed login attempts. Click the button below to reset it.</p>\n" +
            "      </div>\n" +
            "      <div style=\"display: block;text-align: center\">\n" +
            "        <div style=\"font-size: 12px;\n" +
            "           color: #fff;\n" +
            "           font-weight: 100;\n" +
            "           display: inline;\n" +
            "           padding: 10px 15px;\n" +
            "           background: linear-gradient(90deg,  #20a2d0 0%, #50c2ea 100%);\n" +
            "           border-radius: 3px;\n" +
            "           cursor: pointer;margin-left: auto!important;margin-right: auto!important;\">\n" +
            "          <a href=\" " + host + "/api/users/cpassword/" + this.token + "\" style=\"color: #fff !important; text-decoration: unset\">Reset My Password</a>\n" +
            "        </div>\n" +
            "      </div>\n" +
            "      <div>\n" +
            "        <p>If you did not request a password reset, please ignore this email. This password reset is only valid for the next 30 minutes. <br><br> Thanks, <br>eMoldino Support Team</p>\n" +
            "      </div>\n" +
            "    </div>\n" +
            "  </div>\n" +
            "  <div style=\"color: #6c757d!important;text-align: center!important;padding-top:14px;\">\n" +
            "    This is an automated message.If you have any further question, please go to the\n"+
            "          <a href=\" " + host + "/support/customer-support/" + "\" style=\"color:#195bcd !important;\"> Support Center</a>\n" +
            "      or <br> contact us at support@emoldino.com\n" +
            "  </div>\n" +
            "  <div style=\"padding-top:14px;padding-right:70px;padding-bottom:15px;padding-left:70px\">\n" +
            "     <div style=\"text-align:left;font-family:Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;font-size:12px;color:#000000;line-height:23px;mso-line-height:exactly;mso-text-raise:5px;\">\n" +
            "        <p style=\"padding: 0; margin: 0;text-align: center;\">" +
            "           <span style=\"color:#000000;\">" +
            "              eMoldino Co., Ltd., 9F, Daishin Finance Center, 343 Samil-Daero, Jung-gu, " +
            "              Seoul, - <br> 00682, South Korea, +82-70-7678-6388" +
            "           </span>" +
            "        </p>\n" +
            "      </div>\n" +
            "  </div>" +
            "</div>\n" +
            "</body>\n" +
            "</html>";
  }
*/

  @Override
  public String getContent() {
    if (host.charAt(4) == 's') {
      host = host.replaceFirst("s", "");
    }
    String hiRow = "        <p><b>Hi " + this.user.getName() + ",</b> <br> <br> Your account has been locked due to multiple failed login attempts. Click the button below to reset it.</p>\n";
    String linkRow = "          <a href=\" " + host + "/api/users/cpassword/" + this.token + "\" style=\"color: blue !important;\">Reset My Password</a>\n";
    String textR1 = "        <p>If you did not request a password reset, please ignore this email. This password reset is only valid for the next 30 minutes. <br><br> Thanks, <br>eMoldino Support Team</p>\n";
    String linkR2 = "          <a href=\" " + host + "/support/customer-support/" + "\" style=\"color:#195bcd !important;\"> Support Center</a>\n";
    return contentTemp(hiRow, linkRow, textR1, linkR2);
  }

  public static String contentTemp(String hiRow, String linkRow, String textR1, String linkR2) {

    String tempContent = "<!DOCTYPE html>\n" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:v=\"urn:schemas-microsoft-com:vml\">\n" +
            "\n" +
            "<head>\n" +
            "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>\n" +
            "</head>\n" +
            "\n" +
            "<body style=\"background:#f5f5f5\">\n" +
            "<div style=\"max-width: 970px;padding-left: 30px;\">\n" +
            "  <div>\n" +
            "    <div>\n" +
            "      <div>\n" +
            "        <img src=\"https://user-images.githubusercontent.com/69447666/97524542-4a9f6800-19e8-11eb-897a-261949ca1e9a.png\" style=\"padding-top:22px;padding-bottom:12px;height:50px\"/>\n" +
            "      </div>\n" +
            "    </div>\n" +
            "    <div style=\"padding:40px;background:white\">\n" +
            "      <div>\n" +
            hiRow +
            "      </div>\n" +
            "      <div style=\"display: block;text-align: center\">\n" +
            "        <div style=\"font-size: 12px;\n" +
            "           color: #fff;\n" +
            "           font-weight: 100;\n" +
            "           display: inline;\n" +
            "           padding: 10px 15px;\n" +
            "           background: linear-gradient(90deg,  #20a2d0 0%, #50c2ea 100%);\n" +
            "           border-radius: 3px;\n" +
            "           cursor: pointer;margin-left: auto!important;margin-right: auto!important;\">\n" +
            linkRow +
            "        </div>\n" +
            "      </div>\n" +
            "      <div>\n" +
            textR1 +
            "      </div>\n" +
            "    </div>\n" +
            "  </div>\n" +
            "  <div style=\"color: #6c757d!important;text-align: center!important;padding-top:14px;\">\n" +
            "    This is an automated message.If you have any further question, please go to the\n" +
            linkR2 +
            "      or <br> contact us at support@emoldino.com\n" +
            "  </div>\n" +
            "  <div style=\"padding-top:14px;padding-right:70px;padding-bottom:15px;padding-left:70px\">\n" +
            "     <div style=\"text-align:left;font-family:Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;font-size:12px;color:#000000;line-height:23px;mso-line-height:exactly;mso-text-raise:5px;\">\n" +
            "        <p style=\"padding: 0; margin: 0;text-align: center;\">" +
            "           <span style=\"color:#000000;\">" +
            "              eMoldino Co., Ltd., 9F, Seoul Square,  416 Hangang-daero, Jung-gu, " +
            "              Seoul, - <br> 00682, South Korea, +82-70-7678-6388" +
            "           </span>" +
            "        </p>\n" +
            "      </div>\n" +
            "  </div>" +
            "</div>\n" +
            "</body>\n" +
            "</html>";

    return tempContent;
  }
}



