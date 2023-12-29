package vn.com.twendie.avis.api.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.slf4j.MDC;

@Data
@Accessors(fluent = true, chain = true)
@NoArgsConstructor
public class ApiResponse<T> {

	public static final String SUCCESS = "success";
	public static final String FAIL = "fail";
	public static final String ERROR = "error";

	@JsonProperty("status")
	private String status;

	@JsonProperty("data")
	private T data;

	@JsonProperty("trace_id")
	private String trace_id = MDC.get("X-B3-TraceId");

	public ApiResponse(T data) {
		this.data = data;
	}

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(data).status(SUCCESS);
	}

	public static <T> ApiResponse<T> fail(T data) {
		return new ApiResponse<>(data).status(FAIL);
	}

	public static <T> ApiResponse<T> error(T data) {
		return new ApiResponse<>(data).status(ERROR);
	}

}
