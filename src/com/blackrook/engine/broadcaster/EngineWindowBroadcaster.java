/*******************************************************************************
 * Copyright (c) 2016-2018 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.broadcaster;

import com.blackrook.engine.Engine;

/**
 * This interface describes an object that should receive a window listener
 * to attach itself to in order to pass window events along to the EngineListeners.
 * <p>
 * You cannot guarantee the order in which these objects have their methods called on them.
 * @author Matthew Tropiano
 */
public interface EngineWindowBroadcaster
{

	/**
	 * Called by {@link Engine} in order to pass along the event receiver
	 * that is used to broadcast main window events to listening components
	 * in the engine.
	 * @param receiver the listener to use.
	 */
	public void addWindowEventReceiver(EngineWindowEventReceiver receiver);
	
}
