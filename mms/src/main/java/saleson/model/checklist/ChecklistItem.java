//package saleson.model.checklist;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import saleson.model.support.UserDateAudit;
//
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.Id;
//
//@Entity
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
//public class ChecklistItem extends UserDateAudit {
//    @Id
//    @GeneratedValue
//    private Long id;
//    @Column(name = "CHECKLIST_ID")
//    private Long checklistId;
//    private String value;
//
//}
