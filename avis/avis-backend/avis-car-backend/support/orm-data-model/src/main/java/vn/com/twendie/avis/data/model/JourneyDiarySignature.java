package vn.com.twendie.avis.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "journey_diary_signature")
public class JourneyDiarySignature extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(insertable = false, nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_customer_id")
    private MemberCustomer memberCustomer;

    @ManyToOne
    @JoinColumn(name = "journey_diary_id")
    private JourneyDiary journeyDiary;

    @Column(name = "status", nullable = false)
    private Boolean status;

    @Column(name = "time_start")
    private Timestamp timeStart;

    @Column(name = "time_end")
    private Timestamp timeEnd;

    @Column(name = "signature_image_url")
    private String signatureImageUrl;

    @Column(name = "comment")
    private String comment;

}
