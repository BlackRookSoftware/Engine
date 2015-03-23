package com.blackrook.engine.state;

import com.blackrook.engine.struct.InputHandler;
import com.blackrook.engine.struct.UpdateHandler;

/**
 * Defines an engine state that can be pushed or popped from the {@link StateManager}.
 * @author Matthew Tropiano
 */
public interface State extends UpdateHandler, InputHandler
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
