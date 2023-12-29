package saleson.model.checklist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.common.util.StringUtils;
import saleson.model.Company;
import saleson.model.User;
import saleson.model.support.UserDateAudit;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Checklist extends UserDateAudit {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "COMPANY_ID", insertable = false, updatable = false)
    private Long companyId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "COMPANY_ID")
    private Company company;

    private String checklistCode;

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private boolean enabled;

    @Enumerated(EnumType.STRING)
    private ChecklistType checklistType;

    @Enumerated(EnumType.STRING)
    private CheckListObjectType objectType;

    @Lob
    private String checklistItemStr;

    @OneToMany(mappedBy = "checklist")
    private List<ChecklistUser> assignedUsers = new ArrayList<>();

    @OneToMany(mappedBy = "checklist")
    private List<ChecklistCompany> assignedCompanies = new ArrayList<>();

    @Transient
    private User creator;


    public List<String> getChecklistItems(){
        List<String> checklistItems = new ArrayList<>();
        if(!StringUtils.isEmpty(checklistItemStr)){
            String[] itemValues=checklistItemStr.split("\n");
            checklistItems = Arrays.stream(itemValues).collect(Collectors.toList());
        }
        return checklistItems;
    }

    public void setChecklistItems(List<String> checklistItems) {
        if (checklistItems != null && !checklistItems.isEmpty()) {
            this.checklistItemStr = checklistItems.stream().collect(Collectors.joining("\n"));
        }else {
            this.checklistItemStr=null;
        }
    }

    public List<User> getAssignedUserDataList() {
        return assignedUsers.stream().map(ChecklistUser::getUser).collect(Collectors.toList());
    }
}
