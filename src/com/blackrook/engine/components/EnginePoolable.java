package com.blackrook.engine.components;

/**
 * Describes a poolable element, i.e. one that is a pooled instance of a class in
 * order to avoid excessive memory allocations and the expensive process of creation/destruction.
 * @author Matthew Tropiano
 */
public interface EnginePoolable
{
	/**
	 * Returns {@code true} if this object is an active instance in the pool.
	 */
	public boolean isActive();
	
	/**
	 * Returns {@code true} if {@link #isActive()} returns true AND this object
	 * can be returned to be used as a new active instance. Pool policy
	 * shapes whether or not this is considered.
	 */
	public boolean isExpendable();
	
	/**
	 * Returns the age of this object since its creation. The unit of time
	 * for this object is completely arbitrary, but the value is used to
	 * find the oldest object to return if the pool's policy can return an 
	 * expendable object.
	 * @see #isExpendable()
	 */
	public long getAge();
	
	/**
	 * Calling this sets this object's state to a point where it is 
	 * inactive ({@link #isActive()} returns {@code false}).
	 */
	public void deactivate();
	
}
