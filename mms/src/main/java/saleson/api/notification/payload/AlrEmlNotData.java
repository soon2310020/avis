package saleson.api.notification.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlrEmlNotData {
	private String dateString;
	private String logo;
	private String ctaTitle;

	private List<AlrEmlNotItem> alerts = new ArrayList<>();

	private String columnTitle1;
	private String columnTitle2;
	private String columnTitle3;
	private String columnTitle4;

	private String columnValue1;
	private String columnValue2;
	private String columnValue3;
	private String columnValue4;


	public String getColumnValue1() {
		return StringUtils.isEmpty(columnValue1) ? "" : columnValue1;
	}

	public String getColumnValue2() {
		return StringUtils.isEmpty(columnValue2) ? "" : columnValue2;
	}

	public String getColumnValue3() {
		return StringUtils.isEmpty(columnValue3) ? "" : columnValue3;
	}

	public String getColumnValue4() {
		return StringUtils.isEmpty(columnValue4) ? "" : columnValue4;
	}
}
