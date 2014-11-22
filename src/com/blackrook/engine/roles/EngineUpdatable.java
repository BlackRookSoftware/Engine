package com.blackrook.engine.roles;

/**
 * Defines an engine component that is added to the main engine ticker.
 * Most of the time, this will be used on devices. {@link EngineState}s get updated
 * only if they are on the state stack, regardless of their subclassing of this class.
 * @author Matthew Tropiano
 */
public interface EngineUpdatable
{
	/**
	 * Returns true if this object should be updated.
	 */
	public boolean isUpdatable();
	
	/**
	 * Updates this component.
	 * @param tick the current tick in the updater (counts upward each update).
	 * @param currentNanos the current nanotime for the update frame.
	 */
	public void update(long tick, long currentNanos);
	
}
