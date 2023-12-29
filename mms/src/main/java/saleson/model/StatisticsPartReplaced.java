package saleson.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsPartReplaced {
    @Id
    @GeneratedValue
    private Long id;

    @Column(insertable = false, updatable = false)
    private Long statisticsId;

    @ManyToOne
    @JoinColumn(name = "statisticsId")
    private Statistics statistics;

    private Long categoryId;
    private String categoryName;

    private Long projectId; 		// categoryId
    private String projectName;		// categoryName

    private Long partId;
    private String partCode;
    private Integer cavity;

    @CreatedDate
    private Instant createdAt;
}
