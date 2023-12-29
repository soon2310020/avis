package saleson.model.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CounterToolingCode {
	String terminalCode;
	String counterCode;
	String toolingCode;

	@Deprecated
	public CounterToolingCode(String terminalCode, String counterCode) {
		this.terminalCode = terminalCode;
		this.counterCode = counterCode;
	}
}
