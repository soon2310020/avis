package saleson.api.configuration.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.domain.SearchParam;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefurbishmentConfigPayload extends SearchParam {
	private Double mh;
	private Double lm;
	private Double hmMonths;
	private Double mlMonths;
}
