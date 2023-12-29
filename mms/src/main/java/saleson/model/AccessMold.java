package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.model.support.UserDateAudit;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AccessMold extends UserDateAudit {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "COMPANY_ID")
    private Long companyId;
    @Column(name = "MOLD_ID")
    private Long moldId;

    private Long accessCompanyRelationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "COMPANY_ID", insertable = false, updatable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MOLD_ID", insertable = false, updatable = false)
    private Mold mold;

    public AccessMold(Long companyId, Long moldId, Long accessCompanyRelationId) {
        this.companyId = companyId;
        this.moldId = moldId;
        this.accessCompanyRelationId = accessCompanyRelationId;
    }
}
