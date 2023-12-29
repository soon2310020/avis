package vn.com.twendie.avis.mobile.api.model.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(staticName = "create")
@AllArgsConstructor
public class NotificationSettingDTO {

    private Long id;

    private Boolean day;

    private Boolean week;

    private Boolean month;
}
