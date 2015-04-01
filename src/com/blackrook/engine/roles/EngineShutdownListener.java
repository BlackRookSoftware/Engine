package com.blackrook.engine.roles;

import com.blackrook.engine.annotation.component.Ordering;

/**
 * This is a component that has a method invoked on it when the engine is shutting down.
 * These are called after the update ticker is killed.
 * <p>
 * The {@link Ordering} annotation can influence invocation order on this type of object.
 * @author Matthew Tropiano
 */
public interface EngineShutdownListener
{
	/**
	 * Called by Engine when it is shutting down. This is for saving state and
	 * variables and configuration, if necessary, and is called before the
	 * graphics and sound engines get called to shut down the engine, in 
	 * case their state is important.
	 * <p><b>It would not be wise to call this from another method in this class.</b>
	 */
	public void onEngineShutdown();


}
