package com.blackrook.engine.roles;

import com.blackrook.commons.logging.Logger;
import com.blackrook.engine.EngineFileSystem;

/**
 * Describes an object to instantiate at engine startup that generates
 * resources on startup outside of the normal method via Archetext definitions.
 * <p>
 * Classes that implement this are instantiated at resource time to create resources, and can NOT be EngineComponents.
 * The constructor for this class must be a default constructor.
 * @author Matthew Tropiano
 */
public interface EngineResourceGenerator<T extends EngineResource>
{
	/**
	 * Gets the class type that this generates.
	 * This is used to match the generator to use using the class.
	 * @return the class type.
	 */
	public Class<T> getResourceClass();
	
	/**
	 * Called when resources are needed to be created, after the first set of resources
	 * are built from the resource definitions.
	 * @param logger a logger passed to this for logging output. 
	 * @param fileSystem the engine file system to use for generation.
	 * @return an iterable object of resources to add to the resource list.
	 */
	public Iterable<T> createResources(Logger logger, EngineFileSystem fileSystem) throws Exception;
	
	
}
