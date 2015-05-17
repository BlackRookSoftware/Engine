package com.blackrook.engine.roles;

import com.blackrook.engine.Engine;
import com.blackrook.engine.annotation.element.Ordering;


/**
 * This is a component that has a method invoked on it after startup to kickstart the application (set gamestates and such).
 * These are called before the update ticker is started.
 * If more than one class has this, they all have their {@link #onEngineStartup()} method invoked.
 * <p>
 * The {@link Ordering} annotation can influence invocation order on this type of object.
 * @author Matthew Tropiano
 */
public interface EngineStartupListener
{
	/**
	 * Invoked after {@link Engine} startup.
	 */
	public void onEngineStartup();
	
}
