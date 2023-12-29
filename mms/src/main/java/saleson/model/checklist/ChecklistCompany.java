package saleson.model.checklist;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import saleson.model.Company;
import saleson.model.support.DateAudit;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ChecklistCompany  extends DateAudit {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "CHECKLIST_ID", insertable = false, updatable = false)
    private Long checklistId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHECKLIST_ID")
    private Checklist checklist;

    @Column(name = "COMPANY_ID", insertable = false, updatable = false)
    private Long companyId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPANY_ID")
    private Company company;

    public String getCompanyCode() {
        return company.getCompanyCode();
    }

    //company name
    public String getName() {
        return company.getName();
    }
}
