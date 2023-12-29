package saleson.model.clone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.IpType;
import saleson.model.Location;
import saleson.model.support.VersionAudit;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TerminalVersion extends VersionAudit {
    @Id
    @GeneratedValue
    private Long id;
    private String terminalId;
    private String purchasedAt;
    private String memo;
    private Long location;
    private String installedBy;
    private String installedAt;
    private String installationArea;
    private String ipAddress;
    private String subnetMask;
    private String gateway;
    private String dns;
    @Enumerated
    private IpType ipType;
//    private String files;
//    private String fileNames;
}
