/*******************************************************************************
 * Copyright (c) 2016-2018 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.broadcaster;

import com.blackrook.engine.annotation.EngineElement;

/**
 * This class is designed to be the class that implementors of {@link EngineInputBroadcaster}
 * use to fire input events to the engine.
 * <p>
 * This is because there might be different implementations of input libraries, 
 * and the engine would still need to have a consistent way to hear input events. 
 * <p>
 * Basically, if a {@link EngineElement} is supposed to hear input events, it should 
 * fire all of its main input events through this object so that it is broadcast
 * to all listening components. 
 * <p>
 * The Engine uses something called <b>input codes</b> to define actions taken so that the component
 * that is supposed to listen for device actions can broadcast filtered events, or automate them.
 * <p>
 * Input codes should be defined by the layer that reads device input.
 * A good strategy for handling heterogeneous input systems like GUIs are to have those handle system input,
 * and then if the input wasn't handled, pass it along to the Engine.
 * @author Matthew Tropiano 
 */
public interface EngineInputEventReceiver
{
	/**
	 * Should be called to fire an input set/release change event happens to the engine.
	 * @param code the input code.
	 * @param set true if active, false if inactive. 
	 */
	public void fireInputFlag(String code, boolean set);

	/**
	 * Should be called to fire an input value change event to the engine.
	 * This is best used for things like axis changes, throttles, and whatever has a variable input value.
	 * <p>This method is called if the previous input listeners did not handle this.
	 * @param code the input code. 
	 * @param value the value of the input.
	 */
	public void fireInputValue(String code, double value);


}
