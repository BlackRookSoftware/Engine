package com.blackrook.engine.struct;

/**
 * Update handler methods.
 * @author Matthew Tropiano
 */
public interface UpdateHandler
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
