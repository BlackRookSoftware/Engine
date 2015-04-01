package com.blackrook.engine.broadcaster;

import com.blackrook.engine.Engine;
import com.blackrook.engine.EngineWindowEventReceiver;

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
	 * @param listener the listener to use.
	 */
	public void addWindowEventReceiver(EngineWindowEventReceiver listener);
	
}
