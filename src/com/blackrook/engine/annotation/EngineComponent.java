package com.blackrook.engine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.blackrook.engine.annotation.component.CCMD;
import com.blackrook.engine.annotation.component.CVAR;
import com.blackrook.engine.annotation.component.Ordering;

/**
 * Annotation for classes that should be instantiated as singletons for the Engine.
 * These classes can be annotated with {@link CCMD}, {@link CVAR}, and {@link EngineComponentConstructor} annotations.
 * Depending on other component roles, some can be ordered in order to influence listener invocation order.
 * @author Matthew Tropiano
 * @see Ordering
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EngineComponent
{
	/** If true, only instantiated on debug mode. */
	boolean debug() default false;
}
