package vn.com.twendie.avis.api.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GeneralPageResponse<T> implements Serializable {

	private static final long serialVersionUID = -5799995958475485234L;

	@JsonProperty("content") private List<T> content;

	@JsonProperty("total_elements") private long totalElements;

	@JsonProperty("is_last") private boolean last;

	@JsonProperty("is_first") private boolean first;

	@JsonProperty("current_page_elements") private int numberOfElements;

	@JsonProperty("max_page_size") private int pageSize;

	@JsonProperty("current_page_number") private int pageNum;

	@JsonProperty("current_page_offset") private int offset;

	@JsonProperty("total_pages") private int totalPages;

	public static <T> GeneralPageResponse<T> toResponse(Page<T> page) {
		return GeneralPageResponse.<T>builder()
				.content(page.getContent())
				.numberOfElements(page.getNumberOfElements())
				.pageSize(page.getSize())
				.pageNum(page.getNumber() + 1)
				.first(page.isFirst())
				.last(page.isLast())
				.totalElements(page.getTotalElements())
				.offset(page.getNumber())
				.totalPages(page.getTotalPages())
				.build();
	}

}
