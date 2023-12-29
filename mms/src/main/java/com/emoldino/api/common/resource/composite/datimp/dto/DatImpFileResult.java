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
public class DatImpFileResult extends DatImpResultBase {
	private String fileId;
	private String errorMessage;
	@Setter(AccessLevel.PROTECTED)
	private List<DatImpSheetResult> sheetResults = new ArrayList<>();

	public void addSheetResult(DatImpSheetResult sheetResult) {
		if (sheetResult == null) {
			return;
		}
		sheetResults.add(sheetResult);
		setPostCount(getPostCount() + sheetResult.getPostCount());
		setPutCount(getPutCount() + sheetResult.getPutCount());
		setErrorCount(getErrorCount() + sheetResult.getErrorCount());
		setSkippedCount(getSkippedCount() + sheetResult.getSkippedCount());
	}
}
