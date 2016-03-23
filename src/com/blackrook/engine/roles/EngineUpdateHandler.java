/*******************************************************************************
 * Copyright (c) 2016 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.roles;

/**
 * Update handler methods.
 * @author Matthew Tropiano
 */
public interface EngineUpdateHandler
{
	/**
	 * Checks if this object should be updated.
	 * @return true if so, false if not.
	 */
	public boolean isUpdatable();
	
	/**
	 * Updates this component.
	 * @param tick the current tick in the updater (counts upward each update).
	 * @param currentNanos the current nanotime for the update frame.
	 */
	public void update(long tick, long currentNanos);
	
}
