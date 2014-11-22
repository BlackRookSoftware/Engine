package com.blackrook.engine.roles;

import com.blackrook.engine.EngineStateManager;

/**
 * Defines an engine state that can be pushed or popped from the {@link EngineStateManager}.
 * Despite these classes extending {@link EngineUpdatable}, these are not added to the main ticker.
 * @author Matthew Tropiano
 */
public interface EngineState extends EngineUpdatable
{
	/**
	 * Called when this state has been activated or made current.
	 */
	public void enter();
	
	/**
	 * Called when this state is switched away.
	 */
	public void exit();

	/**
	 * Handles a disembodied key press
	 * in the canvas, uncaught by any graphics layers.
	 * @param keyCode the KEY_* keycode.
	 */
	public void onKeyPress(int keyCode);

	/**
	 * Handles a disembodied key release
	 * in the canvas, uncaught by any graphics layers.
	 * @param keyCode the KEY_* keycode.
	 */
	public void onKeyRelease(int keyCode);

	/**
	 * Handles a disembodied key type
	 * in the canvas, uncaught by any graphics layers.
	 * @param keyCode the KEY_* keycode.
	 */
	public void onKeyTyped(int keyCode);

	/**
	 * Handles mouse movement in the canvas.
	 * @param unitsX the amount of units that the mouse moved in the X direction, signed.
	 * @param coordinateX the current canvas coordinate of the mouse, X-axis.
	 * @param unitsY the amount of units that the mouse moved in the Y direction, signed.
	 * @param coordinateY the current canvas coordinate of the mouse, Y-axis.
	 */
	public void onMouseMove(int unitsX, int coordinateX, int unitsY, int coordinateY);

	/**
	 * Handles a disembodied mouse button press
	 * in the canvas, uncaught by any graphics layers.
	 * @param mouseButton the MOUSE_* code.
	 */
	public void onMousePress(int mouseButton);

	/**
	 * Handles a disembodied mouse button release
	 * in the canvas, uncaught by any graphics layers.
	 * @param mouseButton the MOUSE_* code.
	 */
	public void onMouseRelease(int mouseButton);

	/**
	 * Handles a disembodied mouse wheel change
	 * in the canvas, uncaught by any graphics layers.
	 * @param units the amount of units of movement.
	 */
	public void onMouseWheel(int units);

	/**
	 * Handles a disembodied gamepad button press 
	 * in the canvas, uncaught by any graphics layers.
	 * @param gamepadId the gamepad id.
	 * @param gamepadButton the gamepad button.
	 */
	public void onGamepadPress(int gamepadId, int gamepadButton);

	/**
	 * Handles a disembodied gamepad button release 
	 * in the canvas, uncaught by any graphics layers.
	 * @param gamepadId the gamepad id.
	 * @param gamepadButton the gamepad button.
	 */
	public void onGamepadRelease(int gamepadId, int gamepadButton);

	/**
	 * Handles a disembodied gamepad axis change 
	 * in the canvas, uncaught by any graphics layers.
	 * @param gamepadId the gamepad id.
	 * @param gamepadAxisId the gamepad axis id.
	 * @param value the gamepad axis value.
	 */
	public void onGamepadAxisChange(int gamepadId, int gamepadAxisId, float value);

	/**
	 * Handles a disembodied gamepad axis change 
	 * in the canvas, uncaught by any graphics layers.
	 * @param gamepadId the gamepad id.
	 * @param gamepadAxisId the gamepad axis id.
	 * @param positive true if axis value is positive, false if negative.
	 */
	public void onGamepadAxisTap(int gamepadId, int gamepadAxisId, boolean positive);
	

}
