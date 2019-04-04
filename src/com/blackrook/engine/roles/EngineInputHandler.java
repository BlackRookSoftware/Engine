/*******************************************************************************
 * Copyright (c) 2016-2019 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.roles;

/**
 * Input handler methods.
 * @author Matthew Tropiano
 */
public interface EngineInputHandler
{
	/**
	 * Handles a boolean input code setting.
	 * This is best used for things like key presses and mouse buttons and anything else
	 * that has a semantically boolean state.
	 * <p>This method is called if the previous input listeners did not handle this.
	 * @param code the input code.
	 * @param set true if active, false if inactive. 
	 * @return true if this method handled the call, false otherwise. 
	 */
	public boolean onInputSet(String code, boolean set);

	/**
	 * Handles an input value change.
	 * This is best used for things like axis changes, throttles, and whatever has a variable input value.
	 * <p>This method is called if the previous input listeners did not handle this.
	 * @param code the input code. 
	 * @param value the value of the input.
	 * @return true if this method handled the call, false otherwise. 
	 */
	public boolean onInputValue(String code, double value);

}
