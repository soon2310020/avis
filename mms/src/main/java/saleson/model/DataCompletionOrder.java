package saleson.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;
import saleson.common.enumeration.DataRequestStatus;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.UserDateAudit;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class DataCompletionOrder extends UserDateAudit {
    @Id
    @GeneratedValue
    private Long id;

    private String orderId;
    private Integer orderIndex;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "DATA_COMPLETION_ORDER_COMPANY",
            joinColumns = @JoinColumn(name = "ORDER_ID"),
            inverseJoinColumns = @JoinColumn(name = "COMPANY_ID")
    )
    private List<Company> companies = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "DATA_COMPLETION_ORDER_LOCATION",
            joinColumns = @JoinColumn(name = "ORDER_ID"),
            inverseJoinColumns = @JoinColumn(name = "LOCATION_ID")
    )
    private List<Location> locations = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "DATA_COMPLETION_ORDER_CATEGORY",
            joinColumns = @JoinColumn(name = "ORDER_ID"),
            inverseJoinColumns = @JoinColumn(name = "CATEGORY_ID")
    )
    private List<Category> categories = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "DATA_COMPLETION_ORDER_PART",
            joinColumns = @JoinColumn(name = "ORDER_ID"),
            inverseJoinColumns = @JoinColumn(name = "PART_ID")
    )
    private List<Part> parts = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "DATA_COMPLETION_ORDER_MOLD",
            joinColumns = @JoinColumn(name = "ORDER_ID"),
            inverseJoinColumns = @JoinColumn(name = "MOLD_ID")
    )
    private List<Mold> molds = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "DATA_COMPLETION_ORDER_MACHINE",
            joinColumns = @JoinColumn(name = "ORDER_ID"),
            inverseJoinColumns = @JoinColumn(name = "MACHINE_ID")
    )
    private List<Machine> machines = new ArrayList<>();

    private Instant dueDate;
    private String dueDay;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "DATA_COMPLETION_ORDER_USER",
            joinColumns = @JoinColumn(name = "ORDER_ID"),
            inverseJoinColumns = @JoinColumn(name = "USER_ID")
    )
    private List<User> assignedUsers = new ArrayList<>();

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private boolean completedCompany = false;

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private boolean completedLocation = false;

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private boolean completedCategory = false;

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private boolean completedPart = false;

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private boolean completedMold = false;

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private boolean completedMachine = false;

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private boolean completed = false;

    private String sentMailDay;

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private boolean lastReminded = false;

    @Enumerated(EnumType.STRING)
    private DataRequestStatus status;
}
