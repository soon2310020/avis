package com.emoldino.framework.terminology.dictionary;

import javax.persistence.Column;
import javax.persistence.Lob;

public class MField {

	@Lob
	String memo;

	@Column(length = 255)
	String messagingToken;

	@Column(length = 255)
	String moldCode;

	Long moldId;

	@Column(length = 6)
	String month;

}
