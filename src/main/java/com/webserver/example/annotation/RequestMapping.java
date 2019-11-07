package com.webserver.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestMapping {

	public enum Method {
		   GET, POST, PUT, DELETE
	}

	String path() default "";
	Method method() default Method.GET;
	
}
