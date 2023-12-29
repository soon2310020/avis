package com.emoldino.framework.annotation;

import java.lang.annotation.*;

@Target({ ElementType.METHOD })
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
public @interface ApiOptions {
	long longElapsedTimeMillisThreshold() default 0;
}
