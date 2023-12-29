package com.emoldino.api.analysis.resource.base.data.repository.moldchartstat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import javax.persistence.Converter;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter.CycleTime;
import com.emoldino.api.analysis.resource.composite.mldcht.dto.get.MldChtGetOut.MldChtItemPart;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class MoldChartStat {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

//	private String chartType;
	private Long moldId;
//	private Long partId;
//	private Long machineId;

	private String timeScale;
	private String year;
	private String month;
	private String week;
	private String day;
//	private String hour;
//	private String minute;

//	private Double act;
//	private Double wact;

	private Long sc;
	private Double cvt;
	private Double ct;
//	private Double minCt;
//	private Double maxCt;
//	private Double llct;
//	private Double ulct;

//	private Long scVal;
//	private Long cvtVal;
//	private Double ctVal;
//	private Double minCtVal;
//	private Double maxCtVal;
//	private Double llctVal;
//	private Double ulctVal;

//	private Integer tav;
//	private Integer tlo;
//	private Integer thi;
//
//	private Long uptime;

	@CreatedDate
	private Instant createdAt;
	@LastModifiedDate
	private Instant updatedAt;

	@Convert(converter = JsonMldChtItemPartConverter.class)
	private List<MldChtItemPart> parts;

	public void addPart(MldChtItemPart part) {
		if (parts == null) {
			parts = new ArrayList<>();
		}
		parts.add(part);
	}

	@NoArgsConstructor
	@Converter(autoApply = true)
	public static class JsonMldChtItemPartConverter implements AttributeConverter<List<MldChtItemPart>, String> {
		private ObjectMapper objectMapper = new ObjectMapper();

		@Override
		public String convertToDatabaseColumn(List<MldChtItemPart> attribute) {
			try {
				return objectMapper.writeValueAsString(attribute);
			} catch (JsonProcessingException e) {
				return "[]";
			}
		}

		@Override
		public List<MldChtItemPart> convertToEntityAttribute(String dbData) {
			try {
				return objectMapper.registerModule(new JavaTimeModule()).readValue(dbData, new TypeReference<List<CycleTime>>() {
				});
			} catch (Exception e) {
				return new ArrayList<>();
			}
		}
	}
}
