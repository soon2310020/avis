package saleson.common.notification;

import lombok.*;
import saleson.model.MoldMisconfigure;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper=false)
public class MisconfiguredMessage extends MailMessage {
    private List<MoldMisconfigure> moldMisconfigures;

    @Builder
    private MisconfiguredMessage(List<String> receivers, String title, String content, List<MoldMisconfigure> moldMisconfigures){
        super(receivers, title, content);
        this.moldMisconfigures = moldMisconfigures;
    }

    @Override
    public String getTitle() {
        return "[eShotLink] Reset list occurs !";
    }

    @Override
    public String getContent() {
        final String[] content = {"" +
                "<div style=\"border: 1px solid #ccc; border-radius: 7px; max-width: 660px; padding: 20px; font-size: 16px; background: #fbfbfb; min-height: 85px; overflow: hidden;\">\n" +
                "	<img src=\"http://skc.coding.onlinepowers.com/icon-alert.png\" style=\"width: 100px; height: 85px; float: left; margin-right: 15px;\">\n" +
                "	<p style=\"padding: 0; margin:0; padding-top: 15px; line-height: 1.7\">eShotLink is notifying <i style=\"font-weight: bold; color: #2B91AF; text-decoration: underline\">\"Maintenance\" list</i> at " + LocalDateTime.now() + "<br />\n"};
        moldMisconfigures.forEach(moldMisconfigure -> {
            content[0] += "Sensor ID <b>" + moldMisconfigure.getCounterCode() + "</b> status <b> Requested</b>.\n";
        });
        content[0] += "</p>\n" +
                "</div>";
        return content[0];
    }
}
