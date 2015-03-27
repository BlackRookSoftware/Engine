package com.blackrook.engine.annotation.resource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for classes that are engine resources imported via Archetext.
 * By default, if no value is specified, the structure name is the class name, lower camel case ("IntervalType" -&gt; "intervalType").
 * These classes must implement {@link Resource}.
 * @author Matthew Tropiano
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Resource
{
	/** The struct type associated with the class. */
	String value() default "";
}
