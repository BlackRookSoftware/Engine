/*******************************************************************************
 * Copyright (c) 2016-2019 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.annotation.resource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that tells the resource loader to create a searchable spatial index 
 * for the value returned by this method. This annotation must be paired with another one
 * on the same class, with the same index name, and the opposite bound type to have any effect.
 * <p>Attach to a getter method - one that returns a numeric, non-boolean value.
 * @author Matthew Tropiano
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface Interval
{
	/** 
	 * Interval bound. 
	 * @return the bound type for this index. 
	 */
	IntervalBound bound();
	
	/** 
	 * Index name. 
	 * @return the name to use in the resource index. 
	 */
	String value();
}
