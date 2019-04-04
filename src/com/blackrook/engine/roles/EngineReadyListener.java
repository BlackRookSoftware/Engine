/*******************************************************************************
 * Copyright (c) 2016-2019 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.roles;

import com.blackrook.engine.Engine;
import com.blackrook.engine.annotation.element.Ordering;


/**
 * This is a component that has a method invoked on it after startup to kickstart the application (set gamestates and such).
 * These are called before the update ticker is started.
 * If more than one class has this, they all have their {@link #onEngineReady()} method invoked.
 * <p>
 * The {@link Ordering} annotation can influence invocation order on this type of object.
 * @author Matthew Tropiano
 */
public interface EngineReadyListener
{
	/**
	 * Invoked after {@link Engine} startup, which is after the filesystem, 
	 * all resources, components are created, all user/global variables loaded,
	 * devices created, and pending console commands executed, but BEFORE the update ticker is started.
	 */
	public void onEngineReady();
	
}
