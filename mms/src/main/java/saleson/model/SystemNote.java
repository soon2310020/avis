package saleson.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.reflect.TypeToken;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.PageType;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.common.util.DataUtils;
import saleson.dto.SystemNoteParam;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class SystemNote {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private PageType systemNoteFunction;
	@Enumerated(EnumType.STRING)
	private ObjectType objectType;
	private Long objectFunctionId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CREATOR_ID")
	private User creator;

	@Lob
	private String message;
	@Lob
	private String systemNoteParams;

	@Column(name = "PARENT_ID")
	private Long parentId;

	@JoinColumn(name = "PARENT_ID", insertable = false, updatable = false)
	@OneToMany(fetch = FetchType.LAZY)
	@OrderBy("createdAt")
	private List<SystemNote> replies;

	@CreatedDate
	@Column(updatable = false)
	@JsonIgnore
	private Instant createdAt;
	@LastModifiedDate
	@JsonIgnore
	private Instant updatedAt;
	@Convert(converter = BooleanYnConverter.class)
	private boolean deleted;

	@Transient
	private boolean read;

	public CompanyType getCompanyType() {
		return creator == null || creator.getCompany() == null ? null : creator.getCompany().getCompanyType();
	}

	public String getCompanyName() {
		return creator == null || creator.getCompany() == null ? null : creator.getCompany().getName();
	}

	public List<SystemNoteParam> getSystemNoteParamList() {
		if (ObjectUtils.isEmpty(this.systemNoteParams)) {
			return Collections.emptyList();
		}
		try {
			List<SystemNoteParam> list = DataUtils.gson.fromJson(//
					this.systemNoteParams, //
					new TypeToken<ArrayList<SystemNoteParam>>() {
					}.getType()//
			);
			return list == null ? Collections.emptyList() : list;
		} catch (Exception e) {
			// DO NOTHING
		}
		return Collections.emptyList();
	}

	public void setSystemNoteParamList(List<SystemNoteParam> systemNoteParamList) {
		if (systemNoteParamList == null || systemNoteParamList.isEmpty()) {
			this.systemNoteParams = null;
		}
		this.systemNoteParams = DataUtils.gson.toJson(systemNoteParamList);
	}

	public long getNumUnread() {
		long num = 0;
		if (!this.read) {
			num++;
		}
		if (this.replies != null) {
			num += this.replies.stream()//
					.filter(systemNote -> !systemNote.isRead())//
					.count();
		}
		return num;
	}
}
