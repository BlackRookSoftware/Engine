package com.blackrook.engine.components.roles;

import com.blackrook.engine.annotation.EngineComponent;
import com.blackrook.engine.struct.EngineMessage;

/**
 * Any {@link EngineComponent} that implements this interface is automatically
 * added to the message broadcaster. Each call to the implementing method is done
 * in series, so do NOT spend lots of time in the {@link #onEngineMessage(EngineMessage)} call!
 * @author Matthew Tropiano
 */
public interface EngineMessageListener
{
	/**
	 * Called when a message is broadcast.
	 * You cannot guarantee the order in which each component gets called.
	 */
	public void onEngineMessage(EngineMessage message);
	
}
