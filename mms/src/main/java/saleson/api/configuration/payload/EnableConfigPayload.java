package saleson.api.configuration.payload;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.domain.SearchParam;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.ConfigOption;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class EnableConfigPayload extends SearchParam {
	private ConfigCategory configCategory;
	private Boolean enabled;
	private ConfigOption configOption;
	private String value;
}
