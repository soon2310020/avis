package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.support.UserDateAudit;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AccessCompanyRelation extends UserDateAudit {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "COMPANY_ID")
    private Long companyId;
    @Column(name = "COMPANY_PARENT_ID")
    private Long companyParentId;//company parent id

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "COMPANY_ID",referencedColumnName ="COMPANY_ID", insertable = false, updatable = false)
    private AccessHierarchy accessHierarchy;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "COMPANY_PARENT_ID",referencedColumnName ="COMPANY_ID", insertable = false, updatable = false)
    private AccessHierarchy accessHierarchyParent;

    public AccessCompanyRelation(Long companyId, Long companyParentId) {
        this.companyId = companyId;
        this.companyParentId = companyParentId;
    }
}
