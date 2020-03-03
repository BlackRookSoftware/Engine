/*******************************************************************************
 * Copyright (c) 2016-2020 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.roles;

import com.blackrook.engine.annotation.EngineElement;
import com.blackrook.engine.annotation.element.Ordering;

/**
 * Any {@link EngineElement} that implements this interface is automatically
 * added to the message broadcaster. Each call to the implementing method is done
 * in series, so do NOT spend lots of time in the {@link #onEngineMessage(Object, Object ...)} call!
 * <p>
 * The {@link Ordering} annotation can influence invocation order on this type of object.
 * @author Matthew Tropiano
 */
public interface EngineMessageListener
{
	/**
	 * Called when a message is broadcast.
	 * @param type the message type.
	 * @param arguments the arguments passed along with the message.
	 */
	public void onEngineMessage(Object type, Object ... arguments);
	
}
