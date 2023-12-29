package saleson.common.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse {

	private boolean success;

	private String message;

	private Object data;

	public ApiResponse() {
	}

	public ApiResponse(boolean success, String message) {
		this.message = message;
		this.success = success;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() { return data; }

	public void setData(Object data) { this.data = data; }

	public static ApiResponse success() {
		return success("Success");
	}

	public static ApiResponse success(String message) {
		return new ApiResponse(true, message);
	}

	public static ApiResponse success(String message, Object data) {
		return new ApiResponse(true, message, data);
	}

	/*public static ApiResponse error(Exception e) {
		if (e instanceof ApiException) {
			return error(e.getMessage());
		} else {
			return error("SystemError");
		}
	}*/


	public static ApiResponse error() {
		return error("Success");
	}

	public static ApiResponse error(String message) {
		return new ApiResponse(false, message);
	}
}
