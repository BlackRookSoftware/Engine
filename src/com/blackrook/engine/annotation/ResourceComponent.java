package com.blackrook.engine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.blackrook.engine.roles.EngineResource;

/**
 * Annotation for classes that are engine resources imported via Archetext.
 * These classes must implement {@link EngineResource}.
 * @author Matthew Tropiano
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ResourceComponent
{
	/** The struct type associated with the class. */
	String value() default "";
}
