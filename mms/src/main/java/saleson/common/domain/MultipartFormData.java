package saleson.common.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
@NoArgsConstructor
public class MultipartFormData {
	private String payload;
	private MultipartFile[] files;
	private MultipartFile[] secondFiles;
	private MultipartFile[] thirdFiles;
	private MultipartFile[] forthFiles;
}
