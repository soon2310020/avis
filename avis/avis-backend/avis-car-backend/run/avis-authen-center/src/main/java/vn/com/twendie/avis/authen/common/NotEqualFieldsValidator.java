package vn.com.twendie.avis.authen.common;

import java.lang.reflect.Field;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotEqualFieldsValidator implements ConstraintValidator<NotEqualField, Object> {

	private String baseField;
	private String notMatchField;

	@Override
	public void initialize(NotEqualField constraint) {
		baseField = constraint.baseField();
		notMatchField = constraint.notMatchField();
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		try {
			Object baseFieldValue = getFieldValue(value, baseField);
			Object matchFieldValue = getFieldValue(value, notMatchField);
			return baseFieldValue != null && !baseFieldValue.equals(matchFieldValue);
		} catch (Exception e) {
			return false;
		}
	}

	private Object getFieldValue(Object object, String fieldName) throws Exception {
		Class<?> clazz = object.getClass();
		Field passwordField = clazz.getDeclaredField(fieldName);
		passwordField.setAccessible(true);
		return passwordField.get(object);
	}

}
