package com.emoldino.api.common.resource.base.file.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
public @interface FileRelation {
	String prefix();

	String paramName();

	String[] field();

	String versionField() default "";
}
