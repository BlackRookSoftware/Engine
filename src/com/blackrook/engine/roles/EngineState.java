package com.blackrook.engine.roles;

import com.blackrook.engine.EngineStateManager;
import com.blackrook.engine.struct.InputHandler;
import com.blackrook.engine.struct.UpdateHandler;

/**
 * Defines an engine state that can be pushed or popped from the {@link EngineStateManager}.
 * @author Matthew Tropiano
 */
public interface EngineState extends UpdateHandler, InputHandler
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
