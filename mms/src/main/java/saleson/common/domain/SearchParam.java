package saleson.common.domain;

import java.util.List;

import lombok.Data;

@Data
public class SearchParam {
	private Long id;
	private String where;
	private String query;
	private String status;
	private Boolean adminPage;
	private Boolean hideCMS;
	private Boolean inList;
	private Boolean deleted;
	private List<String> selectedFields;
	private String queryMobile;

	private List<Long> filteredIds;

	private String filterCode;

}
