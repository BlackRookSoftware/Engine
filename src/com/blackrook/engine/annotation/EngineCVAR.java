package com.blackrook.engine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for variables accessible to the engine.
 * Variables can be attached to any public getter/setter method or field.
 * <p>
 * Should be paired up in getters/setters. If only on a getter, it is
 * a read-only variable. It cannot only be on a setter.
 * <p> 
 * The getter is the authority on descriptions and archival.
 * @author Matthew Tropiano
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface EngineCVAR
{
	/**
	 * The variable name. If not specified (or blank), this uses the field name, 
	 * or the getter/setter name of the getter/setter method. The name is case-insensitive. 
	 */
	String value() default "";
	
	/**
	 * Is this archived as a setting (if not read-only)?
	 */
	boolean archived() default false;
	
	/**
	 * Variable description.
	 */
	String description() default "Console command.";
	
}
