/*******************************************************************************
 * Copyright (c) 2016 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.state;

import com.blackrook.engine.roles.EngineInputHandler;
import com.blackrook.engine.roles.EngineUpdateHandler;

/**
 * Defines an engine state that can be pushed or popped from the {@link StateManager}.
 * @author Matthew Tropiano
 */
public interface State extends EngineUpdateHandler, EngineInputHandler
{
	/**
	 * Called when this state has been activated or made current.
	 */
	public void enter();
	
	/**
	 * Called when this state is switched away.
	 */
	public void exit();

}
