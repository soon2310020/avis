package com.emoldino.api.analysis.resource.base.data.repository.data;

import java.time.Instant;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.emoldino.api.analysis.resource.base.data.enumeration.DataType;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import saleson.common.hibernate.converter.BooleanYnConverter;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = { @Index(name = "IDX_EXECUTE", columnList = "execute"), })
@DynamicUpdate
public class Data {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private DataType dataType;
	private String terminalId;
	private String readTime;
	private String rawData;
	private int duplicateCount;
	@Convert(converter = BooleanYnConverter.class)
	private boolean execute;

	@CreatedDate
	private Instant createdDate;
	@LastModifiedDate
	private Instant updatedDate;

//	public Data(String terminalId, RawData rawData) {
//		this.terminalId = terminalId;
//		this.rawData = rawData.getData();
//		this.readTime = rawData.getReadtime();
//	}
//
//	public Data(String terminalId, RawData rawData, Instant now) {
//		this(terminalId, rawData);
//		this.createdDate = now;
//		this.updatedDate = now;
//	}

	public String getCreatedDateStr() {
		String str = DateUtils2.format(createdDate, DatePattern.yyyy_MM_dd_HH_mm_ss, Zone.GMT);
		return str;
	}

}
