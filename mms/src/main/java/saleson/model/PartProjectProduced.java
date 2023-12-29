package saleson.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import saleson.model.support.UserDateAudit;

@Deprecated
@Entity
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Table(indexes = { //
		@Index(name = "IDX_PART_ID", columnList = "partId"), //
		@Index(name = "IDX_PROJECT_ID", columnList = "projectId") //
})
public class PartProjectProduced extends UserDateAudit {
	@Id
	@GeneratedValue
	private Long id;

	private Long partId;

	private Long projectId;

	private Long totalProduced;

	private Long totalProducedVal;

	public Long getTotalProduced() {
		return totalProduced != null ? totalProduced : 0;
	}

	public Long getTotalProducedVal() {
		return totalProducedVal != null ? totalProducedVal : 0;
	}
}
