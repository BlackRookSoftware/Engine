package com.blackrook.engine.roles;

import com.blackrook.engine.annotation.component.Component;
import com.blackrook.engine.struct.EngineMessage;

/**
 * Any {@link Component} that implements this interface is automatically
 * added to the message broadcaster. Each call to the implementing method is done
 * in series, so do NOT spend lots of time in the {@link #onEngineMessage(EngineMessage)} call!
 * <p>
 * You cannot guarantee the order in which these objects have their methods called on them.
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
