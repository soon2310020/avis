package com.emoldino.api.integration.resource.composite.inftol.dto;

import java.util.List;

import com.emoldino.api.integration.resource.composite.inftol.enumeration.InfTolGroupBy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfTolDailySummaryIn {
	private List<String> toolingId;
	private String date;
	private InfTolGroupBy groupBy;
}
