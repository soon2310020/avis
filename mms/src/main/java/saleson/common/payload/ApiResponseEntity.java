package saleson.common.payload;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import java.util.LinkedHashMap;
import java.util.Map;

public class ApiResponseEntity extends ResponseEntity<Map<String, Object>> {

	public ApiResponseEntity(HttpStatus status) {
		super(status);
	}

	public ApiResponseEntity(Map<String, Object> body, HttpStatus status) {
		super(body, status);
	}

	public ApiResponseEntity(MultiValueMap<String, String> headers, HttpStatus status) {
		super(headers, status);
	}

	public ApiResponseEntity(Map<String, Object> body, MultiValueMap<String, String> headers, HttpStatus status) {
		super(body, headers, status);
	}

	public static Builder data() {
		return new Builder();
	}


/*
	public static ResponseEntity<Map<String, Object>> error(ApiError error) {
		Builder builder = new Builder();
		builder.status(error.getHttpStatus());
		builder.put("status", error.getHttpStatus().value());
		builder.put("code", error.name());
		builder.put("message", error.getMessage());
		builder.put("description", error.getDescription());
		return builder.error();
	}
*/


	public static class Builder {
		private Map<String, Object> map = new LinkedHashMap<>();
		private HttpStatus status;

		public Builder status(HttpStatus status) {
			this.status = status;
			return this;
		}

		public Builder put(String key, Object value) {
			this.map.put(key, value);
			return this;
		}

		public Builder map(Map<String, Object> map) {
			this.map = map;
			return this;
		}


		public Builder pageContent(Page page) {
			this.map.put("pageContent", page);
			return this;
		}

		public ResponseEntity<Map<String, Object>> ok() {
			if (map.get("pageImpl") != null) {
				Page page = (PageImpl) map.get("pageContent");

				map.remove("pageContent");

				map.put("content", page.getContent());
				map.put("pageable", page.getPageable());
				map.put("last", page.isLast());
				map.put("totalPages", page.getTotalPages());
				map.put("totalElements", page.getTotalElements());
				map.put("size", page.getSize());
				map.put("number", page.getNumber());
				map.put("numberOfElements", page.getNumberOfElements());
				map.put("sort", page.getSort());
				map.put("first", page.isFirst());
			}
			return ResponseEntity.ok(map);
		}

		public ResponseEntity<Map<String, Object>> error() {
				if (this.status == null) {
					status = HttpStatus.BAD_REQUEST;
				}
				return ResponseEntity.status(status).body(map);
		}
	}
}
