package com.emoldino.api.analysis.resource.base.data.repository.dataacceleration;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Converter;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.hibernate.converter.BooleanYnConverter;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = { @Index(name = "IDX_COUNTER_ID", columnList = "counterId"), })
@DynamicUpdate
public class DataAcceleration {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long dataId;
	private Long moldId;

	@Column(length = 20)
	private String terminalId;

	@Column(length = 23)
	private String readTime; // 게이트웨이에서 센서 읽기 성공한 시간

	@Column(length = 20)
	private String counterId; // 카운터 ID

	private String measurementDate; // 가속도 측정 시간

	private Instant rawdataCreatedAt; // RawData 수신 시간 (서버 저장 시간)

	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;
	@LastModifiedDate
	private Instant updatedAt;

	@Column(columnDefinition = "text")
	@Convert(converter = AccelerationDetailConverter.class)
	private List<Acceleration> accelerations = new ArrayList<>();

	private Double similarityMetric;
	private Double similarityMetricHr;
	@Convert(converter = BooleanYnConverter.class)
	private Boolean procChanged;
	// CREATED, UPDATED, UPDATE_ERROR, APPLIED, APPLY_ERROR
	private String procStatus = "CREATED";
	private Long procErrorId;

	@Builder
	public DataAcceleration(Long dataId, String terminalId, String readTime, String counterId, String measurementDate, Instant rawdataCreatedAt, Instant createdAt) {
		this.dataId = dataId;
		this.terminalId = terminalId;
		this.readTime = readTime;
		this.counterId = counterId;
		this.measurementDate = measurementDate;
		this.rawdataCreatedAt = rawdataCreatedAt;
		this.createdAt = createdAt;
	}

	public void addAcceleration(Acceleration acceleration) {
		this.accelerations.add(acceleration);
	}

	public String getRawdataCreatedDate() {
		String str = DateUtils2.format(rawdataCreatedAt, DatePattern.yyyy_MM_dd_HH_mm_ss, Zone.GMT);
		return str;
	}

	@Converter(autoApply = true)
	public static class AccelerationDetailConverter implements AttributeConverter<List<Acceleration>, String> {
		private ObjectMapper objectMapper = new ObjectMapper();

		@Override
		public String convertToDatabaseColumn(List<Acceleration> attribute) {
			try {
				return objectMapper.writeValueAsString(attribute);
			} catch (JsonProcessingException e) {
				return "[]";
			}
		}

		@Override
		public List<Acceleration> convertToEntityAttribute(String dbData) {
			try {
				return objectMapper.registerModule(new JavaTimeModule()).readValue(dbData, new TypeReference<List<Acceleration>>() {
				});
			} catch (Exception e) {
				return new ArrayList<>();
			}
		}
	}

}
