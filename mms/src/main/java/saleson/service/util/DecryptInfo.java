package saleson.service.util;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class DecryptInfo {
	private String decryptText;
	private String key;
	private String iv;
}
