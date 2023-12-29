package saleson.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.PageType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BroadcastNotificationDTO {
    private Long id;
    private Long userId;
    private String message;
    private List<String> valueList = new ArrayList<>();
    private List<String> infors = new ArrayList<>();
    private String notificationType;//alert/system note
    private boolean isRead = false;
    private Instant createdAt;
    private PageType systemFunction;
    private String title;

}
