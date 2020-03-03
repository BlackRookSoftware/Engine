/*******************************************************************************
 * Copyright (c) 2016-2020 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.roles;

import com.blackrook.engine.Engine;
import com.blackrook.engine.receiver.EngineInputEventReceiver;

/**
 * This interface describes an object that should receive an input listener
 * to attach itself to in order to pass input events along to the active EngineInputListeners (and EngineStates).
 * <p>
 * The Engine uses something called <b>input codes</b> to define actions taken so that the component
 * that is supposed to listen for device actions can broadcast filtered events, or automate them.
 * <p>
 * <b>Input codes</b> should be defined by the layer that reads device input.
 * A good strategy for handling heterogeneous input systems like GUIs are to have those handle system input,
 * and then if the input wasn't handled, pass it along to the Engine.
 * <p>
 * You cannot guarantee the order in which these objects have their methods called on them.
 * @author Matthew Tropiano
 */
public interface EngineInputBroadcaster
{

	/**
	 * Called by {@link Engine} in order to pass along the event receiver
	 * that is used to broadcast input events to listening components
	 * in the engine.
	 * @param receiver the receiver to use.
	 */
	public void addInputReceiver(EngineInputEventReceiver receiver);
	
}
