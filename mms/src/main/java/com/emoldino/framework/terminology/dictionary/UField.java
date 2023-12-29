package com.emoldino.framework.terminology.dictionary;

import java.time.Instant;

import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

public class UField {

	@LastModifiedDate
	Instant updatedAt;

	@LastModifiedBy
	Long updatedBy;

	Long userId;
}
