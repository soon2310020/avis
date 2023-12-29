package saleson.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
public class TabTableData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long refId;

	@Column(name = "tab_table_id", insertable = false, updatable = false)
	private Long tabTableId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "tab_table_id")
	private TabTable tabTable;

	@JsonIgnore
	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;
	@JsonIgnore
	@LastModifiedDate
	private Instant updatedAt;

	public TabTableData(Long refId, Long tabTableId, TabTable tabTable) {
		this.refId = refId;
		this.tabTableId = tabTableId;
		this.tabTable = tabTable;
	}

	public TabTableData(Long refId, TabTable tabTable) {
		this.refId = refId;
		this.tabTable = tabTable;
	}
}
