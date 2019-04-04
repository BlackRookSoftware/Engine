/*******************************************************************************
 * Copyright (c) 2016-2019 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.roles;

import com.blackrook.commons.logging.Logger;
import com.blackrook.engine.EngineFileSystem;
import com.blackrook.engine.EngineResources;
import com.blackrook.engine.annotation.EngineElement;
import com.blackrook.engine.annotation.element.Ordering;

/**
 * Describes an object to instantiate at engine startup that generates
 * resources on startup outside of the normal method via Archetext definitions.
 * The generators get called BEFORE the ArcheText definitions are parsed.
 * <p>
 * Classes that implement this are instantiated at resource time to create resources, and cannot be an {@link EngineElement}.
 * The constructor for this class must be a default constructor.
 * <p>
 * The {@link Ordering} annotation can influence invocation order on this type of object.
 * @author Matthew Tropiano
 */
public interface EngineResourceGenerator
{
	/**
	 * Called when resources are needed to be created, before the first set of resources
	 * are built from the resource definitions.
	 * @param logger a logger passed to this for logging output. 
	 * @param fileSystem the engine file system to use for generation.
	 * @param resources the resource bank to add to.
	 */
	public void createResources(Logger logger, EngineFileSystem fileSystem, EngineResources resources);
	
}
