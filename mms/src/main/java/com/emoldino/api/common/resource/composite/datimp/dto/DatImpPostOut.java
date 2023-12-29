package com.emoldino.api.common.resource.composite.datimp.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DatImpPostOut extends DatImpResultBase {
	@Setter(AccessLevel.PROTECTED)
	List<DatImpFileResult> fileResults = new ArrayList<>();

	public void addFileResult(DatImpFileResult fileResult) {
		if (fileResult == null) {
			return;
		}
		fileResults.add(fileResult);
		setPostCount(getPostCount() + fileResult.getPostCount());
		setPutCount(getPutCount() + fileResult.getPutCount());
		setErrorCount(getErrorCount() + fileResult.getErrorCount());
		setSkippedCount(getSkippedCount() + fileResult.getSkippedCount());
	}
}
