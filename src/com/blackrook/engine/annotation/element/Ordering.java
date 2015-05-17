package com.blackrook.engine.annotation.element;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to be used on Engine Role objects to influence the ordering.
 * Sorting is from lowest to highest value.
 * @author Matthew Tropiano
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Ordering
{
	/** Ordering bias. */
	int value() default 0;
}
