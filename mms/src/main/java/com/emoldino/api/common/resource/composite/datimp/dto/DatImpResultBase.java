package com.emoldino.api.common.resource.composite.datimp.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class DatImpResultBase {
	@Setter(AccessLevel.PROTECTED)
	private long postCount;
	@Setter(AccessLevel.PROTECTED)
	private long putCount;
	@Setter(AccessLevel.PROTECTED)
	private long errorCount;
	@Setter(AccessLevel.PROTECTED)
	private long skippedCount;

	public void post() {
		postCount++;
	}

	public void put() {
		putCount++;
	}

	public void error() {
		errorCount++;
	}

	public void skip() {
		skippedCount++;
	}

	public long getTotalCount() {
		long count = postCount + putCount + errorCount + skippedCount;
		return count;
	}

	public long getSuccessCount() {
		return postCount + putCount;
	}
}
