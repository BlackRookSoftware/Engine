package com.blackrook.engine.roles;

/**
 * Describes a device started by the engine after object creation and configuration
 * script execution.
 * @author Matthew Tropiano
 */
public interface EngineDevice
{
	/**
	 * Returns a friendlier name for this device.
	 * This cannot return null.
	 */
	public String getDeviceName();
	
	/**
	 * Returns true if this device was "created" and can be destroyed.
	 */
	public boolean isActive();
	
	/**
	 * Creates this device.
	 * <p>
	 * This is called (AND SHOULD ONLY BE CALLED) by the Engine after component
	 * singletons are created and the heavyweight devices (Graphics, Sound, ... etc) need
	 * creating.
	 * <p>
	 * You cannot guarantee the order in which these devices have this method called on them.
	 */
	public boolean create();
	
	/**
	 * Destroys this device.
	 * <p>
	 * This is called (AND SHOULD ONLY BE CALLED) by the Engine when this device is
	 * restarted or the engine is shutting down.
	 * <p>
	 * You cannot guarantee the order in which these devices have this method called on them.
	 */
	public boolean destroy();
	
}
