/*******************************************************************************
 * Copyright (c) 2016 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.roles;

/**
 * Describes a resource descriptor that the engine stores and indexes in its resource banks.
 * In Engine, these are classes that are engine resources imported via Archetext definitions.
 * <p>Classes that implement this can NOT be EngineComponents.
 * @author Matthew Tropiano
 */
public interface EngineResource
{
	/** Reference to an empty string array. */
	public final String[] EMPTY_STRING_ARRAY = new String[0];
	
	/**
	 * The identity of this resource.
	 * This name describes this resource's uniqueness - if another
	 * resource is found that uses this name, it is replaced on import. 
	 * @return the identity of the resource.
	 */
	public String getId();
	
	/**
	 * Returns the tags that this resource uses which abstractly
	 * may define its greater purpose.
	 * @return the tags on this resource.
	 */
	public String[] getTags();
	
}
