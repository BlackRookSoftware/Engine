package com.blackrook.engine;

import com.blackrook.commons.Common;
import com.blackrook.commons.Reflect;
import com.blackrook.commons.SingletonManager;
import com.blackrook.commons.hash.Hash;
import com.blackrook.commons.list.List;
import com.blackrook.engine.annotation.EngineComponent;
import com.blackrook.engine.components.EngineConsoleManager;

/**
 * The main engine, created as the centerpoint of the communication between components
 * or as a main mediator between system components.
 * @author Matthew Tropiano
 */
public class Engine
{
	/** Engine singleton map. */
	private SingletonManager engineSingletons;
	/** Engine console manager. */
	private EngineConsoleManager consoleManager;
	
	/**
	 * Creates the engine and all of the other stuff.
	 * @param config the configuration to use for engine setup.
	 */
	public Engine(EngineConfig config)
	{
		engineSingletons = new SingletonManager();
		consoleManager = engineSingletons.get(EngineConsoleManager.class);
		
		for (Class<?> componentClass : getComponentClasses(config))
		{
			// TODO: test for important class types here.
			consoleManager.addEntries(engineSingletons.get(componentClass));
		}
		
	}
	
	/**
	 * Adds engine singletons to the engine singleton manager.
	 * @param config the configuration to use for engine setup.
	 */
	protected Iterable<Class<?>> getComponentClasses(EngineConfig config)
	{
		List<Class<?>> outList = new List<Class<?>>();
		
		// Scan for singletons to instantiate.
		Hash<String> packageMap = new Hash<String>();
		for (String className : Reflect.getClasses(Common.getPackagePathForClass(Engine.class)))
			packageMap.put(className);
		for (String className : Reflect.getClasses(config.getApplicationPackageRoot()))
			packageMap.put(className);
		
		for (String className : packageMap)
		{
			Class<?> clz = null;
			try {
				clz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("This should not have happened.", e);
			}
			
			EngineComponent ecomponent = clz.getAnnotation(EngineComponent.class);
			if (ecomponent == null)
				continue;
			
			outList.add(clz);
		}
		
		return outList;
	}
	
}
