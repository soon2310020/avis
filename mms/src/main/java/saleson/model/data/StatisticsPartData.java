package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class StatisticsPartData {

	private Long partId;
	private Long projectId;
	private Integer producedQuantity;
	private Boolean isUpper;

	@QueryProjection
	public StatisticsPartData(Long partId, Integer producedQuantity){
		this.partId = partId;
		this.producedQuantity = producedQuantity;
	}

	@QueryProjection
	public StatisticsPartData(Long partId, Long projectId, Integer producedQuantity){
		this.partId = partId;
		this.projectId = projectId;
		this.producedQuantity = producedQuantity;
	}
}
