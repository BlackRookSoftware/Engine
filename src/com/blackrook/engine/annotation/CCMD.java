package com.blackrook.engine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for console command entry points.
 * Console commands can be attached to any public method that is on an EngineComponent.
 * @author Matthew Tropiano
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CCMD
{
	/**
	 * The command name. If not specified (or blank), this uses the method
	 * name. The command name is case-insensitive. 
	 */
	String value() default "";
	
	/**
	 * Usage blurb.
	 */
	String[] usage() default {};
	
	/**
	 * Command description.
	 */
	String description() default "Console command.";
	
}
