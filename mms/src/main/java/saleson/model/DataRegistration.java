package saleson.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import saleson.common.enumeration.DataRequestStatus;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.UserDateAudit;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Getter
@Setter
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class DataRegistration extends UserDateAudit {

    @Id
    @GeneratedValue
    private Long id;

    private String requestId;
    private Integer requestIndex;

    private Integer companyNumber;
    private Integer locationNumber;
    private Integer categoryNumber;
    private Integer partNumber;
    private Integer moldNumber;
    private Integer machineNumber;

    private Instant dueDate;
    private String dueDay;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "DATA_REGISTRATION_USER",
            joinColumns = @JoinColumn(name = "REQUEST_ID"),
            inverseJoinColumns = @JoinColumn(name = "USER_ID")
    )
    private List<User> assignedUsers = new ArrayList<>();

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private boolean completed = false;

    private String sentMailDay;

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private boolean lastReminded = false;

    @Enumerated(EnumType.STRING)
    private DataRequestStatus status;

    @JsonIgnore
    @OneToMany(mappedBy = "dataRegistration")
    private List<DataRegistrationObject> dataRegistrationObjects = new ArrayList<>();
}
