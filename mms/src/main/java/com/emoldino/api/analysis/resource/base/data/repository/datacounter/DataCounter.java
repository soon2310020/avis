package com.emoldino.api.analysis.resource.base.data.repository.datacounter;

import java.math.BigDecimal;
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

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = { @Index(name = "IDX_COUNTER_ID", columnList = "counterId"), })
public class DataCounter {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long dataId;

	@Column(length = 20)
	private String terminalId; // 게이트웨이ID

	@Column(length = 23)
	private String readTime; // 게이트웨이에서 센서 읽기 성공한 시간

	@Column(length = 20)
	private String counterId; // 카운터ID

	@Column(length = 14)
	private String shotStartTime; // Shot시작시간

	@Column(length = 14)
	private String shotEndTime; // shot종료시간

	private int shotCount; // 최종shot수

	@Column(length = 2)
	private String batteryStatus; // 배터리상태

	@Column(length = 2)
	private String status; // 상태정보(통신여부)

	private String temperature; // 온도

	private BigDecimal modeOpenTime; // 최빈 mold open time

	private BigDecimal modeCycleTime; // 최빈 cycleTime (indexing table 에서 확인)

	private Instant rawdataCreatedAt; // RawData 수신 시간 (서버 저장 시간)

	@CreatedDate
	private Instant createdAt; // 데이터 저장 시간 (서버 저장시간)

	@Column(length = 5000)
	@Convert(converter = JsonCycleTimeConverter.class)
	private List<CycleTime> cycleTimes = new ArrayList<>();

	@Builder
	public DataCounter(String terminalId, String readTime, String counterId, String shotStartTime, String shotEndTime, int shotCount, String batteryStatus, String status,
			String temperature, BigDecimal modeOpenTime, BigDecimal modeCycleTime) {
		this.terminalId = terminalId;
		this.readTime = readTime;
		this.counterId = counterId;
		this.shotStartTime = shotStartTime;
		this.shotEndTime = shotEndTime;
		this.shotCount = shotCount;
		this.batteryStatus = batteryStatus;
		this.status = status;
		this.temperature = temperature;
		this.modeOpenTime = modeOpenTime;
		this.modeCycleTime = modeCycleTime;
	}

	public void addCycleTimeIndex(CycleTime indexTable) {
		this.cycleTimes.add(indexTable);
	}

	public String getRawdataCreatedDate() {
		String str = DateUtils2.format(rawdataCreatedAt, DatePattern.yyyy_MM_dd_HH_mm_ss, Zone.GMT);
		return str;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CycleTime {
		private String id;
		private BigDecimal cycleTime;
	}

	@NoArgsConstructor
	@Converter(autoApply = true)
	public static class JsonCycleTimeConverter implements AttributeConverter<List<CycleTime>, String> {
		private ObjectMapper objectMapper = new ObjectMapper();

		@Override
		public String convertToDatabaseColumn(List<CycleTime> attribute) {
			try {
				return objectMapper.writeValueAsString(attribute);
			} catch (JsonProcessingException e) {
				return "[]";
			}
		}

		@Override
		public List<CycleTime> convertToEntityAttribute(String dbData) {
			try {
				return objectMapper.registerModule(new JavaTimeModule()).readValue(dbData, new TypeReference<List<CycleTime>>() {
				});
			} catch (Exception e) {
				return new ArrayList<>();
			}
		}
	}

}
