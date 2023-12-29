package saleson.model.rejectedPartRate;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.hibernate.converter.BooleanYnConverter;

import javax.persistence.*;

@Entity
@Data
@Builder
@NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class RejectedPartDetails {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "PRODUCED_PART_ID", insertable = false, updatable = false)
    private Long producedPartId;

    @JsonIgnore
    @ManyToOne
    private ProducedPart producedPart;

    private String reason;
    private Integer rejectedAmount;

    @Transient
    private Double rejectedRate;

    @Column(length = 1)
    @Convert(converter = BooleanYnConverter.class)
    private Boolean isDefault;

    private String remark;

    @QueryProjection
    public RejectedPartDetails(String reason, Integer rejectedAmount) {
        this.reason = reason;
        this.rejectedAmount = rejectedAmount;
    }
}
