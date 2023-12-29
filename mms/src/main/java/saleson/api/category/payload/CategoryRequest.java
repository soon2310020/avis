package saleson.api.category.payload;

import org.springframework.web.multipart.MultipartFile;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CategoryRequest extends CategoryRequestLite {
	//upload
	private MultipartFile[] projectImage;
}
