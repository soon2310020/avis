package com.stg.service.dto.validator;

import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LessValidator implements ConstraintValidator<Less, Object> {

	private String field;
	private String dependField;
	private boolean orEqual;

	@Override
	public void initialize(Less annotation) {
		field = annotation.field();
		dependField = annotation.dependField();
		orEqual = annotation.orEqual();
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}

		boolean valid = false;

		BeanWrapperImpl wrapper = new BeanWrapperImpl(value);
		Object fieldValue = wrapper.getPropertyValue(field);
		Object dependFieldValue = wrapper.getPropertyValue(dependField);

		if (fieldValue instanceof Integer && dependFieldValue instanceof Integer) {
			if (orEqual) {
				valid = (Integer) fieldValue <= (Integer) dependFieldValue;
			} else {
				valid = (Integer) fieldValue <= (Integer) dependFieldValue;
			}
		}

		return valid;
	}

}
