package saleson.service.transfer;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferPostIn {
	private boolean batch;
	private List<String> content;

	public TransferPostIn(List<String> content) {
		this.content = content;
	}
}
