/*******************************************************************************
 * Copyright (c) 2016-2021 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.state;

import com.blackrook.engine.handler.EngineInputHandler;
import com.blackrook.engine.handler.EngineUpdateHandler;

/**
 * Defines an engine state that can be pushed or popped from the {@link StateManager}.
 * @author Matthew Tropiano
 */
public interface State extends EngineUpdateHandler, EngineInputHandler
{
	/**
	 * Called when this state has been activated or made current.
	 * @param config the configuration for this state.
	 */
	public void enter(StateConfig config);
	
	/**
	 * Called when this state is switched away.
	 */
	public void exit();

}
