package com.emoldino.api.analysis.resource.base.data.repository.statisticslite;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "STATISTICS")
public class StatisticsLite {
	@Id
	private Long id;
	private Long moldId;
	private Double ct;
	private Double ctVal;
	private String day;
}
