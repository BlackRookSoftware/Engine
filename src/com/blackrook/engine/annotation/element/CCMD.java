package com.blackrook.engine.annotation.element;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.blackrook.engine.annotation.Element;

/**
 * Annotation for console command entry points.
 * Console commands can be attached to any public method that is on an {@link Element}.
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

	/**
	 * If true, this is exposed in DEBUG mode only.
	 */
	boolean debug() default false;
	
}
