package saleson.model.checklist;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import saleson.model.User;
import saleson.model.support.DateAudit;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ChecklistUser extends DateAudit {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "CHECKLIST_ID", insertable = false, updatable = false)
    private Long checklistId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHECKLIST_ID")
    private Checklist checklist;

    @Column(name = "USER_ID", insertable = false, updatable = false)
    private Long userId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    public String getName() {
        return user.getDisplayName();
    }
    public Long getCompanyId() {
        return user.getCompanyId();
    }
    public String getCompanyCode() {
        return user.getCompany() != null ? user.getCompany().getCompanyCode() : null;
    }
}
