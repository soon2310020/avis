package saleson.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(indexes = {
		@Index(name="IDX_SP_01", columnList = "partId"),
		@Index(name="IDX_SP_02", columnList = "partCode")
})
@EntityListeners(AuditingEntityListener.class)
public class StatisticsPart {
	@Id
	@GeneratedValue
	private Long id;

	@Column(insertable = false, updatable = false)
	private Long statisticsId;

	@ManyToOne
	@JoinColumn(name = "statisticsId")
	private Statistics statistics;

	private Long categoryId;
	private String categoryName;

	private Long projectId; 		// categoryId
	private String projectName;		// categoryName

	private Long partId;
	private String partCode;
	private Integer cavity;

	@CreatedDate
	private Instant createdAt;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getStatisticsId() {
		return statisticsId;
	}

	public void setStatisticsId(Long statisticsId) {
		this.statisticsId = statisticsId;
	}

	public Statistics getStatistics() {
		return statistics;
	}

	public void setStatistics(Statistics statistics) {
		this.statistics = statistics;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public Long getPartId() {
		return partId;
	}

	public void setPartId(Long partId) {
		this.partId = partId;
	}

	public String getPartCode() {
		return partCode;
	}

	public void setPartCode(String partCode) {
		this.partCode = partCode;
	}

	public Integer getCavity() {
		return cavity;
	}

	public void setCavity(Integer cavity) {
		this.cavity = cavity;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
