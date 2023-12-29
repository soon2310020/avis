package saleson.common.notification;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import static org.unbescape.html.HtmlEscape.escapeHtml4;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ContentMailMessage extends MailMessage {
    @Builder
    public ContentMailMessage(List<String> receivers, String title, String content) {
        super(receivers, title, content);
    }
}
