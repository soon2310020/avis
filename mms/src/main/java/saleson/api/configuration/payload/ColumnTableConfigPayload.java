package saleson.api.configuration.payload;

import java.util.List;

import com.emoldino.api.common.resource.base.option.dto.TableColumnConfigItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.domain.SearchParam;
import saleson.common.enumeration.PageType;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColumnTableConfigPayload extends SearchParam {
	private PageType pageType;
//	private Long userId;
//	private String columnName;
//	private Boolean enabled;
	private List<TableColumnConfigItem> columnConfig;
}
