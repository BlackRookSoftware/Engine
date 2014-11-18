package com.blackrook.engine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that tells the resource loader to create a searchable index for the
 * value returned by this method.
 * <p>Attach to a getter method.
 * <p>By default, the index name is taken from the getter method name, without "get".
 * @author Matthew Tropiano
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Indexed
{
	/** Index name. */
	String value() default "";
}
