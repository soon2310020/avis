package vn.com.twendie.avis.data.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ContractCostTypeId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @ManyToOne
    @JoinColumn(name = "cost_type_id", nullable = false, insertable = false, updatable = false)
    private CostType costType;

}
