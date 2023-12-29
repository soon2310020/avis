package saleson.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.ObjectType;
import saleson.common.hibernate.converter.BooleanYnConverter;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
public class TabTable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private Long userId;

	@Enumerated(EnumType.STRING)
	private ObjectType objectType;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private boolean isShow = true;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private boolean deleted = false;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private Boolean isDefaultTab = false;

	@Transient
	private Long totalItem;

	@JsonIgnore
	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;
	@JsonIgnore
	@LastModifiedDate
	private Instant updatedAt;

	public TabTable(String name, ObjectType objectType, Boolean isDefaultTab) {
		this.name = name;
		this.objectType = objectType;
		this.isDefaultTab = isDefaultTab;
	}

	public TabTable(String name, Long userId, ObjectType objectType, Boolean isDefaultTab) {
		this.name = name;
		this.userId = userId;
		this.objectType = objectType;
		this.isDefaultTab = isDefaultTab;
	}

	@QueryProjection
	public TabTable(TabTable tabTable, Long totalItem) {
		this.id = tabTable.getId();
		this.name = tabTable.getName();
		this.userId = tabTable.getUserId();
		this.objectType = tabTable.getObjectType();
		this.isShow = tabTable.isShow();
		this.deleted = tabTable.isDeleted();
		this.isDefaultTab = tabTable.getIsDefaultTab();
		this.totalItem = totalItem;
	}
}
