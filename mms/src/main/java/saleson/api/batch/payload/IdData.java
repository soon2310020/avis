package saleson.api.batch.payload;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IdData {
	private Long mainObjectId;
	private Long referenceObjectId;

	@QueryProjection
	public IdData(Long mainObjectId, Long referenceObjectId) {
		this.mainObjectId = mainObjectId;
		this.referenceObjectId = referenceObjectId;
	}
}
