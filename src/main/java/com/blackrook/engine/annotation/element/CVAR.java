/*******************************************************************************
 * Copyright (c) 2016-2020 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.annotation.element;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.blackrook.engine.annotation.EngineElement;

/**
 * Annotation for variables accessible to the engine.
 * Variables can be attached to any public getter/setter method or field on {@link EngineElement}s.
 * <p>
 * Should be paired up in getters/setters. If only on a getter, it is
 * a read-only variable. It cannot only be on a setter.
 * <p>
 * The getter is the authority on descriptions and archival.
 * @author Matthew Tropiano
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface CVAR
{
	/**
	 * The variable name. If not specified (or blank), this uses the field name, 
	 * or the getter/setter name of the getter/setter method. The name is case-insensitive. 
	 * @return the variable name to use. 
	 */
	String value() default "";
	
	/**
	 * Checks if this archived as a setting (if not read-only)?
	 * @return true if so, false if not.
	 */
	boolean archived() default false;
	
	/**
	 * Checks if this a globally-saved variable (as opposed to user)?
	 * @return true if so, false if not.
	 */
	boolean global() default false;
	
	/**
	 * Variable description.
	 * @return the variable description. 
	 */
	String description() default "Console variable.";
	
}
