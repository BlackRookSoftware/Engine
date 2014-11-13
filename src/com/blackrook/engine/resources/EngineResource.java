package com.blackrook.engine.resources;

/**
 * Describes a resource descriptor 
 * that the engine stores and indexes in its resource banks.
 * @author Matthew Tropiano
 */
public interface EngineResource
{
	/**
	 * The identity of this resource.
	 * This name describes this resource's uniqueness - if another
	 * resource is found that uses this name, it is replaced on import. 
	 */
	public String getId();
	
	/**
	 * Returns the tags that this resource uses which abstractly
	 * may define its greater purpose.
	 */
	public String[] getTags();
	
}
