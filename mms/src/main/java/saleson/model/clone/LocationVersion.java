package saleson.model.clone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.VersionAudit;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationVersion extends VersionAudit {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @Column(length = 20)
    private String locationCode;
    @Column
    private String address;
    @Lob
    private String memo;
    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private boolean enabled;
    @Column(name = "COMPANY_ID")
    private Long companyId;
}
