package com.blackrook.engine.input;

import com.blackrook.input.InputComponent;
import com.blackrook.input.annotation.ComponentBinding;

// TODO: Change fields to reflect xbox controllers.
public class XBoxController
{
	/** Gamepad axis X. */
	@ComponentBinding(InputComponent.AXIS_X)
	public float x;
	/** Gamepad axis Y. */
	@ComponentBinding(InputComponent.AXIS_Y)
	public float y;
	/** Gamepad axis Z. */
	@ComponentBinding(InputComponent.AXIS_Z)
	public float z;
	/** Gamepad axis RX. */
	@ComponentBinding(InputComponent.AXIS_RX)
	public float rx;
	/** Gamepad axis RY. */
	@ComponentBinding(InputComponent.AXIS_RY)
	public float ry;
	/** Gamepad axis RZ. */
	@ComponentBinding(InputComponent.AXIS_RZ)
	public float rz;
	/** Gamepad axis POV. */
	@ComponentBinding(InputComponent.AXIS_POV)
	public float pov;
	
	/** Gamepad button 0. */
	@ComponentBinding(InputComponent.BUTTON_0)
	public boolean a;
	/** Gamepad button 1. */
	@ComponentBinding(InputComponent.BUTTON_1)
	public boolean _1;
	/** Gamepad button 2. */
	@ComponentBinding(InputComponent.BUTTON_2)
	public boolean _2;
	/** Gamepad button 3. */
	@ComponentBinding(InputComponent.BUTTON_3)
	public boolean _3;
	/** Gamepad button 4. */
	@ComponentBinding(InputComponent.BUTTON_4)
	public boolean _4;
	/** Gamepad button 5. */
	@ComponentBinding(InputComponent.BUTTON_5)
	public boolean _5;
	/** Gamepad button 6. */
	@ComponentBinding(InputComponent.BUTTON_6)
	public boolean _6;
	/** Gamepad button 7. */
	@ComponentBinding(InputComponent.BUTTON_7)
	public boolean _7;
	/** Gamepad button 8. */
	@ComponentBinding(InputComponent.BUTTON_8)
	public boolean _8;
	/** Gamepad button 9. */
	@ComponentBinding(InputComponent.BUTTON_9)
	public boolean _9;
	/** Gamepad button 10. */
	@ComponentBinding(InputComponent.BUTTON_10)
	public boolean _10;
	/** Gamepad button 11. */
	@ComponentBinding(InputComponent.BUTTON_11)
	public boolean _11;

}
