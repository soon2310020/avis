package saleson.model.clone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TerminalVersionDto {
    private Long id;
    private String terminalId;
    private String purchasedAt;
    private String memo;
    private Long location;
    private String locationName;
    private String installedBy;
    private String installedAt;
    private String installationArea;
    private String ipAddress;
    private String ipTypeString;
//    private String files;
//    private List<String> fileNameArray;
}
