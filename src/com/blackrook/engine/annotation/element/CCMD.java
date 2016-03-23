/*******************************************************************************
 * Copyright (c) 2016 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.annotation.element;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.blackrook.engine.annotation.EngineElement;

/**
 * Annotation for console command entry points.
 * Console commands can be attached to any public method that is on an {@link EngineElement}.
 * @author Matthew Tropiano
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CCMD
{
	/**
	 * The command name. If not specified (or blank), this uses the method
	 * name. The command name is case-insensitive.
	 * @return the command name to use. 
	 */
	String value() default "";
	
	/**
	 * Usage blurb.
	 * @return the usage parameters. 
	 */
	String[] usage() default {};
	
	/**
	 * Command description.
	 * @return the console command description. 
	 */
	String description() default "Console command.";

	/**
	 * Checks if this is exposed in DEBUG mode only.
	 * @return true if so, false if not.
	 */
	boolean debug() default false;
	
}
