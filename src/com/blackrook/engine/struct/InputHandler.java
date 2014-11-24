package com.blackrook.engine.struct;

/**
 * Input handler methods.
 * @author Matthew Tropiano
 */
public interface InputHandler
{
	/**
	 * Handles a boolean input code setting.
	 * This is best used for things like key presses and mouse buttons and anything else
	 * that has a semantically boolean state.
	 * <p>This method is called if the previous input listeners did not handle this.
	 * @param code the input code.
	 * @param set true if active, false if inactive. 
	 * @return true if this method handled the call and should NOT pass it along to other listeners, false otherwise. 
	 */
	public boolean onInputSet(int code, boolean set);

	/**
	 * Handles an input value change.
	 * This is best used for things like axis changes, throttles, and whatever has a variable input value.
	 * <p>This method is called if the previous input listeners did not handle this.
	 * @param code the input code. 
	 * @param value the value of the input.
	 * @return true if this method handled the call and should NOT pass it along to other listeners, false otherwise. 
	 */
	public boolean onInputValue(int code, double value);

}
