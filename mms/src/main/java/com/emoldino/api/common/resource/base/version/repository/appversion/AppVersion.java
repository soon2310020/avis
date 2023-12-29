package com.emoldino.api.common.resource.base.version.repository.appversion;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Converter;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.emoldino.framework.repository.ListAttributeConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.UserLite;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class AppVersion {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String appCode;
	private String version;
	private String releaseDate;

	@JsonIgnore
	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private boolean enabled = true;

	@JsonIgnore
	@CreatedBy
	@Column(updatable = false)
	private Long createdBy;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "createdBy", insertable = false, updatable = false)
	private UserLite createdByUser;

	public String getCompanyName() {
		UserLite user = getCreatedByUser();
		return user == null ? null : user.getCompanyName();
	}

	public String getCreatedByUserName() {
		UserLite user = getCreatedByUser();
		return user == null ? null : user.getName();
	}

	public String getCreatedByUserEmail() {
		UserLite user = getCreatedByUser();
		return user == null ? null : user.getEmail();
	}

	public String getCreatedByUserMobileNumber() {
		UserLite user = getCreatedByUser();
		return user == null ? null : user.getMobileNumber();
	}

	public String getCreatedByUserDepartment() {
		UserLite user = getCreatedByUser();
		return user == null ? null : user.getDepartment();
	}

	public String getCreatedByUserPosition() {
		UserLite user = getCreatedByUser();
		return user == null ? null : user.getPosition();
	}

	@JsonIgnore
	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;

	@JsonIgnore
	@LastModifiedBy
	private Long updatedBy;
	@JsonIgnore
	@LastModifiedDate
	private Instant updatedAt;

	@Convert(converter = AppVersionDetailConverter.class)
	private List<AppVersionDetail> items = new ArrayList<>();

	public List<AppVersionDetail> getItems() {
		return items == null ? new ArrayList<>() : items;
	}

	public void addItem(AppVersionDetail item) {
		if (this.items == null) {
			this.items = new ArrayList<>();
		}
		this.items.add(item);
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AppVersionDetail {
		private String description;
	}

	@NoArgsConstructor
	@Converter(autoApply = true)
	public static class AppVersionDetailConverter extends ListAttributeConverter<AppVersionDetail> {

	}
}
