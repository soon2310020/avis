package saleson.restdocs.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

@Data
public class PageDto {
	private List content = new ArrayList();

	@JsonIgnore
	private Pageable pageable;


	private Boolean first;
	private Boolean last;
	private Long totalPages;
	private Long totalElements;
	private Long size;
	private Long number;
}
