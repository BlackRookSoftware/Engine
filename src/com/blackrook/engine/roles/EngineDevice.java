/*******************************************************************************
 * Copyright (c) 2016-2018 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.roles;

import com.blackrook.engine.annotation.element.Ordering;

/**
 * Describes a device started by the engine after object creation and configuration
 * script execution.
 * <p>
 * The {@link Ordering} annotation can influence invocation order on this type of object.
 * @author Matthew Tropiano
 */
public interface EngineDevice
{
	/**
	 * Gets a friendlier name for this device.
	 * This cannot return null.
	 * @return the "friendly" name for the device.
	 */
	public String getDeviceName();
	
	/**
	 * Checks if this device was "created" and can be destroyed.
	 * @return true if so, false if not.
	 */
	public boolean isDeviceActive();
	
	/**
	 * Creates this device.
	 * <p>
	 * This is called (AND SHOULD ONLY BE CALLED) by the Engine after component
	 * singletons are created and the heavyweight devices (Graphics, Sound, ... etc) need
	 * creating.
	 * @return true if created successfully, false if not.
	 */
	public boolean createDevice();
	
	/**
	 * Destroys this device.
	 * <p>
	 * This is called (AND SHOULD ONLY BE CALLED) by the Engine when this device is
	 * restarted or the engine is shutting down.
	 * @return true if destroyed successfully, false if not.
	 */
	public boolean destroyDevice();
	
}
