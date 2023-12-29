package saleson.model.clone;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CompanyType;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.VersionAudit;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class CompanyVersion extends VersionAudit {

    @Id
    @GeneratedValue
    private Long id;
    @Enumerated(EnumType.STRING)
    private CompanyType companyType;

    private String companyCode;
    private String name;
    private String address;
    private String manager;
    private String phone;
    private String email;

    @Lob
    private String memo;

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private boolean enabled;

}
