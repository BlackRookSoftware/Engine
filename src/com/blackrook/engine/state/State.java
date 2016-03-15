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
