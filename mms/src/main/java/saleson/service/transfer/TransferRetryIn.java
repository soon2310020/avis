package saleson.service.transfer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRetryIn {
	private Long id;
	private String fromTime;
	private String toTime;
}
