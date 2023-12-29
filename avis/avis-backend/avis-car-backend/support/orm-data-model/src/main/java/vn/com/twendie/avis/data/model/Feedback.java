package vn.com.twendie.avis.data.model;

import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;

@Audited
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "feedback")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Feedback extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "content", length = 1000)
    private String content;

    @ManyToOne
    @JoinColumn(name = "created_by_id", referencedColumnName = "id")
    private User createdBy;

}
