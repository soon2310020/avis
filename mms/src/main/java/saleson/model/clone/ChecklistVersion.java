package saleson.model.clone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.Company;
import saleson.model.support.VersionAudit;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class ChecklistVersion extends VersionAudit {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "COMPANY_ID", insertable = false, updatable = false)
    private Long companyId;
    private String checklistCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "COMPANY_ID")
    private Company company;

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private boolean enabled;

    @Lob
    private String checklistItemStr;

}
