package saleson.api.configuration.payload;

import java.util.List;

import com.emoldino.api.common.resource.base.option.dto.OPConfigItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllConfigPayload {
	List<GeneralConfigPayload> generalConfigPayloads;
	List<OPConfigItem> opConfigPayloads;
	AlertConfigPayload alertConfigPayloads;
	RefurbishmentConfigPayload refurbishmentConfigPayload;
}
