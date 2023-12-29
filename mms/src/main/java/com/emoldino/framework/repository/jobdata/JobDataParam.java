package com.emoldino.framework.repository.jobdata;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Deprecated
@Entity
@Data
public class JobDataParam {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long jobDataId;

	// LOGIC, AFTER_LOGIC, THROWS_LOGIC
	private String paramFor;
	private Integer position;
	private String name;
	private String dataType;
	private String value;
}
