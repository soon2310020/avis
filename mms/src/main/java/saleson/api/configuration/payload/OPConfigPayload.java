package saleson.api.configuration.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.domain.SearchParam;
import saleson.common.enumeration.EquipmentType;
import saleson.common.enumeration.OperatingStatus;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OPConfigPayload extends SearchParam {
	private EquipmentType equipmentType;
	private OperatingStatus operatingStatus;
}
