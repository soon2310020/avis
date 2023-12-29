package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.UserDateAudit;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AccessHierarchy extends UserDateAudit {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "COMPANY_ID", insertable = false, updatable = false, unique = true)
    private Long companyId;
    private Long level;//root 0
    //    private Long companyParentId;//company parent id
    //    private String parentIdListStr;
    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private boolean multipleParent;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "COMPANY_ID")
    private Company company;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPANY_ID", referencedColumnName = "COMPANY_ID", insertable = false, updatable = false)
    private List<AccessCompanyRelation> accessCompanyParentRelations = new ArrayList<>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPANY_PARENT_ID", referencedColumnName = "COMPANY_ID", insertable = false, updatable = false)
    private List<AccessCompanyRelation> accessCompanyChildRelations = new ArrayList<>();

    @Transient
    @JsonIgnore
    private List<Long> parentIdListTemp;

    public List<Long> getParentIdList() {
        List<Long> resList = new ArrayList<>();
        try {
            if (accessCompanyParentRelations != null)
                resList = accessCompanyParentRelations.stream().map(a -> a.getCompanyParentId()).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resList;
    }

    public List<AccessCompanyRelation> generationAccessCompanyParentRelation(List<Long> parentIdList) {
        if (parentIdList != null) {
            Map<Long, AccessCompanyRelation> accessCompanyRelationMap = accessCompanyParentRelations.stream().collect(Collectors.toMap(x -> x.getCompanyParentId(), x -> x));
            accessCompanyParentRelations = parentIdList.stream().map(id -> {
                        if (accessCompanyRelationMap.get(id) != null) return accessCompanyRelationMap.get(id);
                        return new AccessCompanyRelation(companyId, id);
                    }
            ).collect(Collectors.toList());
        } else {
            accessCompanyParentRelations = new ArrayList<>();
        }
        if (parentIdList == null || parentIdList.size() <= 1) {
            this.multipleParent = false;
        } else {
            this.multipleParent = true;
        }
        return accessCompanyParentRelations;
    }

    public List<Long> getChildIdList() {
        List<Long> resList = new ArrayList<>();
        try {
            if (accessCompanyChildRelations != null)
                resList = accessCompanyChildRelations.stream().map(a -> a.getCompanyId()).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resList;
    }
}
