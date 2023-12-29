//package saleson.model;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import saleson.model.support.UserDateAudit;
//
//import javax.persistence.*;
//import java.util.List;
//
//@Getter
//@Setter
//@Entity
//@NoArgsConstructor
//@AllArgsConstructor
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
//public class ShiftConfig extends UserDateAudit {
//
//    @Id
//    @GeneratedValue(strategy= GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "LOCATION_ID", insertable = false, updatable = false)
//    private Long locationId;
//
//    @JsonIgnore
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "LOCATION_ID")
//    private Location location;
//
//    @JsonIgnore
//    @OneToMany(mappedBy = "shiftConfig")
//    private List<DayShift> dayShifts;
//}
