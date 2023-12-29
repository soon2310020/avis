package saleson.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.Frequent;
import saleson.model.support.UserDateAudit;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Builder
public class MachineOee extends UserDateAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "MACHINE_ID")
	private Long machineId;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MACHINE_ID", insertable = false, updatable = false)
	private Machine machine;

	@Column(name = "MOLD_ID")
	private Long moldId;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MOLD_ID", insertable = false, updatable = false)
	private Mold mold;

	private String day;
	private String hour;
	private String tenMinute;

	private Long partProduced;
	private Long rejectedPart;
	private Long partProducedVal;

	private Double fa;
	private Double fp;
	private Double fq;

	@Enumerated(EnumType.STRING)
	private Frequent periodType;

	private Integer downtimeDuration;

	public Double getOee() {
		double availability = fa == null ? 0 : fa;
		double performance = fp == null ? 0 : fp;
		double quality = fq == null ? 0 : fq;
		return (availability / 100) * (performance / 100) * (quality / 100) * 100;
	}
}
