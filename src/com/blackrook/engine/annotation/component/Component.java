package com.blackrook.engine.annotation.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for classes that should be instantiated as singletons for the Engine.
 * These classes can be annotated with {@link CCMD}, {@link CVAR}, and {@link ComponentConstructor} annotations.
 * @author Matthew Tropiano
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component
{
	
}
