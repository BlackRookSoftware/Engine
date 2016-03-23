/*******************************************************************************
 * Copyright (c) 2016 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.roles;

import com.blackrook.engine.annotation.EngineElement;
import com.blackrook.engine.annotation.element.Ordering;
import com.blackrook.engine.struct.EngineMessage;

/**
 * Any {@link EngineElement} that implements this interface is automatically
 * added to the message broadcaster. Each call to the implementing method is done
 * in series, so do NOT spend lots of time in the {@link #onEngineMessage(EngineMessage)} call!
 * <p>
 * The {@link Ordering} annotation can influence invocation order on this type of object.
 * @author Matthew Tropiano
 */
public interface EngineMessageListener
{
	/**
	 * Called when a message is broadcast.
	 * You cannot guarantee the order in which each component gets called.
	 * @param message the message sent.
	 */
	public void onEngineMessage(EngineMessage message);
	
}
