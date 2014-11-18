package com.blackrook.engine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that tells the resource loader to create a searchable spatial index 
 * for the value returned by this method. This annotation must be paired with another one
 * on the same class, with the same index name, and the opposite bound type to have any effect.
 * <p>Attach to a getter method - one that returns a numeric, non-boolean value.
 * @author Matthew Tropiano
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Interval
{
	/** Interval bound. */
	IntervalBound bound();
	/** Index name. */
	String value();
}
