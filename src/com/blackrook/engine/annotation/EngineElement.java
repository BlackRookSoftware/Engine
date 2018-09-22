/*******************************************************************************
 * Copyright (c) 2016-2018 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.blackrook.engine.annotation.element.CCMD;
import com.blackrook.engine.annotation.element.CVAR;
import com.blackrook.engine.annotation.element.Ordering;

/**
 * Annotation for classes that should be instantiated as singletons for the Engine.
 * These classes can be annotated with {@link CCMD}, {@link CVAR}, and {@link EngineElementConstructor} annotations.
 * Depending on other component roles, some can be ordered in order to influence listener invocation order.
 * @author Matthew Tropiano
 * @see Ordering
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EngineElement
{
	/** 
	 * Checks if this element is only instantiated on debug mode.
	 * @return true if so, false if not. 
	 */
	boolean debug() default false;
}
