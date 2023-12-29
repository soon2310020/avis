package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.DateAudit;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserInvite  extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @NotBlank
    @Size(max = 50)
    private String email;

    @Column(name = "COMPANY_ID", insertable = false, updatable = false)
    private Long companyId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "COMPANY_ID")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User sender;

    @CreatedDate
    private Instant inviteAt;
    private Instant joinedAt;
    @Convert(converter = BooleanYnConverter.class)
    private Boolean joined;

    @Column(unique = true)
    private Integer hashCode;

    @Convert(converter = BooleanYnConverter.class)
    private Boolean enabled;

    @Transient
    private Integer index;

    @Transient
    private List<String> emailList;
//    @Transient
//    private String rowId;

}
