package com.emoldino.api.common.resource.composite.datimp.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.ObjectUtils;

import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.util.ValueUtils;

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
public class DatImpSheetResult extends DatImpResultBase {
	private String sheetName;
	@Setter(AccessLevel.PRIVATE)
	private List<DatImpError> errors = new ArrayList<>();

	@Setter(AccessLevel.PROTECTED)
	private List<Long> postIds = new ArrayList<>();
	@Setter(AccessLevel.PROTECTED)
	private List<Long> putIds = new ArrayList<>();

	private Integer firstDataRowNo;
	private Integer lastRowDataRead;

	public void setPostCount(long postCount) {
		super.setPostCount(postCount);
	}

	public void setPutCount(long putCount) {
		super.setPutCount(putCount);
	}

	public void setSkippedCount(long skippedCount) {
		super.setSkippedCount(skippedCount);
	}

	public long getErrorCount() {
		return errors.size();
	}

	public void error(Integer rowNo, Integer colNo, Exception e) {
		AbstractException ae = ValueUtils.toAe(e, null);
		addError(new DatImpError(rowNo, colNo, ae.getCodeMessage()));
	}

	public void addError(DatImpError cellError) {
		super.error();
		this.errors.add(cellError);
	}

	public void addErrors(List<DatImpError> cellErrors) {
		if (ObjectUtils.isEmpty(cellErrors)) {
			return;
		}
		cellErrors.stream().forEach(cellError -> addError(cellError));
	}

	public void addPostId(Long postId) {
		this.postIds.add(postId);
	}

	public void addPutId(Long putId) {
		this.putIds.add(putId);
	}
}
