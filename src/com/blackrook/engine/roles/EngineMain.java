package com.blackrook.engine.roles;

import com.blackrook.engine.Engine;


/**
 * This is a component that has a method invoked on it after startup to kickstart the application (set gamestates and such).
 * These are called before the update ticker is started.
 * If more than one class has this, they all have their {@link #start()} method invoked. The order is not guaranteed.
 * @author Matthew Tropiano
 */
public interface EngineMain
{
	/**
	 * Invoked after {@link Engine} startup.
	 */
	public void start();
	
}
