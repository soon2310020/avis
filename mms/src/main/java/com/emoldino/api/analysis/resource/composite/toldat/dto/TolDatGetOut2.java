package com.emoldino.api.analysis.resource.composite.toldat.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.emoldino.api.analysis.resource.base.data.util.MoldDataUtils;
import com.emoldino.api.analysis.resource.composite.toldat.dto.TolDatGetOut.MldDatItem;
import com.emoldino.api.analysis.resource.composite.toldat.dto.TolDatGetOut2.MldDatItem2;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;
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

@SuppressWarnings("serial")
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TolDatGetOut2 extends PageImpl<MldDatItem2> {
	private String moldCode;
	private String date;

	public TolDatGetOut2(List<MldDatItem2> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}

	public TolDatGetOut2(TolDatGetOut output) {
		this(//
				output.getContent()//
						.stream()//
						.map(item -> new MldDatItem2(item))//
						.collect(Collectors.toList()), //
				output.getPageable(), //
				output.getTotalElements()//
		);
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@JsonInclude(Include.NON_NULL)
	public static class MldDatItem2 {
		private Integer hourlyNo;
		private String hour;
		private String time;
		private BigDecimal approvedCt;
		private BigDecimal wact;
		private BigDecimal actualCt;
		private BigDecimal ctLl2;
		private BigDecimal ctLl1;
		private BigDecimal ctUl1;
		private BigDecimal ctUl2;
		private BigDecimal temperature;
		private BigDecimal injectionTime;
		private BigDecimal packingTime;
		private BigDecimal coolingTime;

		public boolean isIdle() {
			return MoldDataUtils.isCtIdle(actualCt);
		}

		public boolean isOutOfL1() {
			return MoldDataUtils.isCtOutOfL1(actualCt, ctLl2, ctLl1, ctUl1, ctUl2);
		}

		public boolean isOutOfL2() {
			return MoldDataUtils.isCtOutOfL2(actualCt, ctLl2, ctUl2);
		}

		public MldDatItem2(MldDatItem item) {
			this(//
					null, //
					DateUtils2.toOtherPattern(item.getTime(), DatePattern.yyyyMMddHHmmss, DatePattern.HH), //
					DateUtils2.toOtherPattern(item.getTime(), DatePattern.yyyyMMddHHmmss, DatePattern.HH_mm_ss), //
					ValueUtils.toBigDecimal(item.getApprovedCt(), null), //
					ValueUtils.toBigDecimal(item.getWact(), null), //
					ValueUtils.toBigDecimal(item.getActualCt(), null), //
					ValueUtils.toBigDecimal(item.getCtLl2(), null), //
					ValueUtils.toBigDecimal(item.getCtLl1(), null), //
					ValueUtils.toBigDecimal(item.getCtUl1(), null), //
					ValueUtils.toBigDecimal(item.getCtUl2(), null), //
					ValueUtils.toBigDecimal(item.getTemperature(), null), //
					ValueUtils.toBigDecimal(item.getInjectionTime(), null), //
					ValueUtils.toBigDecimal(item.getPackingTime(), null), //
					ValueUtils.toBigDecimal(item.getCoolingTime(), null)//
			);
			int hourlyNo = 1;
			String prevHour = (String) ThreadUtils.getProp("MldDatItem2.hour");
			if (ValueUtils.equals(hour, prevHour)) {
				hourlyNo = ValueUtils.toInteger(ThreadUtils.getProp("MldDatItem2.hourlyNo"), 0);
				if (!ValueUtils.equals(time, (String) ThreadUtils.getProp("MldDatItem2.prevTime"))) {
					hourlyNo++;
				}
			} else {
				ThreadUtils.setProp("MldDatItem2.hour", hour);
			}
			this.hourlyNo = hourlyNo;
			ThreadUtils.setProp("MldDatItem2.hourlyNo", hourlyNo);
			ThreadUtils.setProp("MldDatItem2.prevTime", time);
		}
	}
}
