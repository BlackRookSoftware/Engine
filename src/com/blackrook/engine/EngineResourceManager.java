package com.blackrook.engine;

import com.blackrook.commons.hash.HashMap;
import com.blackrook.engine.annotation.EngineComponent;
import com.blackrook.engine.annotation.EngineComponentConstructor;
import com.blackrook.engine.resources.EngineResourceList;

/**
 * The resource manager class.
 * @author Matthew Tropiano
 */
@EngineComponent
public class EngineResourceManager
{
	
	/** Internal map. */
	private HashMap<Class<?>, EngineResourceList<?>> classMap;
	
	/**
	 * Creates an engine resource manager.
	 */
	@EngineComponentConstructor
	public EngineResourceManager()
	{
		classMap = new HashMap<Class<?>, EngineResourceList<?>>();
		// TODO: Finish.
	}
	
	
	
	
}
