package com.emoldino.api.analysis.resource.composite.proana.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.emoldino.api.analysis.resource.composite.proana.dto.ProAnaGetOut.ProAnaItem;
import com.emoldino.framework.dto.ListOut;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ProAnaGetOut extends ListOut<ProAnaItem> {
	private Long moldId;
	private String moldCode;

	private List<ProAnaPartsItem> parts;

	public long totalShotCount;

	public ProAnaGetOut(List<ProAnaItem> content) {
		super(content);
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ProAnaItem {
		private String time;

		private int shotCount;		
       
        private BigDecimal injectionTime;

		private BigDecimal refInjectionTime;
		@JsonInclude(Include.NON_NULL)
		private BigDecimal injectionTimeUl;
		@JsonInclude(Include.NON_NULL)
		private BigDecimal injectionTimeLl;
		@JsonInclude(Include.NON_NULL)
		private BigDecimal injectionMinTime;
		@JsonInclude(Include.NON_NULL)
		private BigDecimal injectionMaxTime;
		@JsonIgnore
		private ArrayList<Double> injectionTimeList;
		
//		@JsonIgnore
//		private double totalPackingTime;
//
//		public BigDecimal getPackingTime() {
//			return totalPackingTime == 0d || totalShotCount == 0d ? null //
//					: new BigDecimal(totalPackingTime / totalShotCount).setScale(2, RoundingMode.HALF_UP);
//		}
		
		private BigDecimal packingTime;

		private BigDecimal refPackingTime;
		@JsonInclude(Include.NON_NULL)
		private BigDecimal packingTimeUl;
		@JsonInclude(Include.NON_NULL)
		private BigDecimal packingTimeLl;
		@JsonInclude(Include.NON_NULL)
		private BigDecimal packingMinTime;
		@JsonInclude(Include.NON_NULL)
		private BigDecimal packingMaxTime;
		@JsonIgnore
		private ArrayList<Double> packingTimeList;

//		@JsonIgnore
//		private double totalCoolingTime;
//
//		public BigDecimal getCoolingTime() {
//			return totalCoolingTime == 0d || totalShotCount == 0d ? null //
//					: new BigDecimal(totalCoolingTime / totalShotCount).setScale(2, RoundingMode.HALF_UP);
//		}
		
		private BigDecimal coolingTime;

		private BigDecimal refCoolingTime;
		@JsonInclude(Include.NON_NULL)
		private BigDecimal coolingTimeUl;
		@JsonInclude(Include.NON_NULL)
		private BigDecimal coolingTimeLl;
		@JsonInclude(Include.NON_NULL)
		private BigDecimal coolingMinTime;
		@JsonInclude(Include.NON_NULL)
		private BigDecimal coolingMaxTime;
		@JsonIgnore
		private ArrayList<Double> coolingTimeList;

//		@JsonIgnore
//		private double totalInjectionPressure;
//
//		public BigDecimal getInjectionPressure() {
//			return totalInjectionPressure == 0d || totalShotCount == 0d ? null //
//					: new BigDecimal(totalInjectionPressure / totalShotCount).setScale(2, RoundingMode.HALF_UP);
//		}
		
		private BigDecimal injectionPressure;

		private BigDecimal refInjectionPressure;
		@JsonInclude(Include.NON_NULL)
		private BigDecimal injectionPressureUl;
		@JsonInclude(Include.NON_NULL)
		private BigDecimal injectionPressureLl;
		@JsonInclude(Include.NON_NULL)
		private BigDecimal injectionMinPressure;
		@JsonInclude(Include.NON_NULL)
		private BigDecimal injectionMaxPressure;
		@JsonIgnore
		private ArrayList<Double> injectionPressureList;

//		@JsonIgnore
//		private double totalPackingPressure;
//
//		public BigDecimal getPackingPressure() {
//			return totalPackingPressure == 0d || totalShotCount == 0d ? null //
//					: new BigDecimal(totalPackingPressure / totalShotCount).setScale(2, RoundingMode.HALF_UP);
//		}
		
		private BigDecimal packingPressure;

		private BigDecimal refPackingPressure;
		@JsonInclude(Include.NON_NULL)
		private BigDecimal packingPressureUl;
		@JsonInclude(Include.NON_NULL)
		private BigDecimal packingPressureLl;
		@JsonInclude(Include.NON_NULL)
		private BigDecimal packingMinPressure;
		@JsonInclude(Include.NON_NULL)
		private BigDecimal packingMaxPressure;
		@JsonIgnore
		private ArrayList<Double> packingPressureList;

		private boolean abnormal;		
		private long abnormalCount;
		
		boolean itAbnormal;
		boolean ptAbnormal;
		boolean ctAbnormal;
		boolean ipAbnormal;
		boolean ppAbnormal;
	}
}
