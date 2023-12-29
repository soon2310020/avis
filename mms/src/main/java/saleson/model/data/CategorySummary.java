package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;

public class CategorySummary {

	private Long categoryId;
	private String categoryName;
	private Long projectCount;
	private Long partCount;
	private Long moldCount;


	@QueryProjection
	public CategorySummary(String categoryName, Long projectCount, Long partCount, Long moldCount){
		this.categoryName = categoryName;
		this.projectCount = projectCount;
		this.partCount = partCount;

		this.moldCount = moldCount;
	}

	public CategorySummary(Long categoryId, String categoryName, Long projectCount, Long partCount, Long moldCount){
		this.categoryId = categoryId;
		this.categoryName = categoryName;
		this.projectCount = projectCount;
		this.partCount = partCount;

		this.moldCount = moldCount;
	}

	public CategorySummary(String categoryName){
		this.categoryName = categoryName;
		this.projectCount = projectCount;
		this.partCount = partCount;
		this.moldCount = moldCount;
	}

	public Long getCategoryId(){ return categoryId; }

	public void setCategoryId(Long categoryId){ this.categoryId = categoryId; }

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public Long getProjectCount() {
		return projectCount;
	}

	public void setProjectCount(Long projectCount) {
		this.projectCount = projectCount;
	}

	public Long getPartCount() {
		return partCount;
	}

	public void setPartCount(Long partCount) {
		this.partCount = partCount;
	}

	public Long getMoldCount() {
		return moldCount;
	}

	public void setMoldCount(Long moldCount) {
		this.moldCount = moldCount;
	}
}
