/*******************************************************************************
 * Copyright (c) 2016 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.broadcaster;

import com.blackrook.engine.annotation.EngineElement;

/**
 * This class is designed to be the class that implementors of {@link EngineMessageBroadcaster}
 * use to fire messages to the engine and its visible components.
 * <p>
 * Basically, if a {@link EngineElement} is supposed to hear messages, it should 
 * fire its messages through this object so that it is broadcast to all listening components. 
 * @author Matthew Tropiano 
 */
public interface EngineMessageReceiver
{
	/**
	 * Should be called to fire a message to broadcast through the engine.
	 * @param type the message type.
	 * @param arguments the arguments to pass along with the message.
	 */
	public void sendMessage(Object type, Object ... arguments);

}
