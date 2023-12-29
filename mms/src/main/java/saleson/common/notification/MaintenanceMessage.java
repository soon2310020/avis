package saleson.common.notification;

import lombok.*;
import saleson.model.MoldMaintenance;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper=false)
public class MaintenanceMessage extends MailMessage {
    private List<MoldMaintenance> moldMaintenances;

    @Builder
    private MaintenanceMessage(List<String> receivers, String title, String content, List<MoldMaintenance> moldMaintenanceList){
        super(receivers, title, content);
        this.moldMaintenances = moldMaintenanceList;
    }

    @Override
    public String getTitle() {
        return "[eShotLink] Maintenance list occurs !";
    }

    @Override
    public String getContent() {
        final String[] content = {"" +
                "<div style=\"border: 1px solid #ccc; border-radius: 7px; max-width: 660px; padding: 20px; font-size: 16px; background: #fbfbfb; min-height: 85px; overflow: hidden;\">\n" +
                "	<img src=\"http://skc.coding.onlinepowers.com/icon-alert.png\" style=\"width: 100px; height: 85px; float: left; margin-right: 15px;\">\n" +
                "	<p style=\"padding: 0; margin:0; padding-top: 15px; line-height: 1.7\">eShotLink is notifying <i style=\"font-weight: bold; color: #2B91AF; text-decoration: underline\">\"Maintenance\" list</i> at " + LocalDateTime.now() + "<br />\n"};
        moldMaintenances.forEach(moldMaintenance -> {
            content[0] += "Tooling ID <b>" + moldMaintenance.getMold().getEquipmentCode() + "</b> status <b>" + moldMaintenance.getMaintenanceStatus() + "</b>.\n";
        });
        content[0] += "</p>\n" +
                "</div>";
        return content[0];
    }
}
