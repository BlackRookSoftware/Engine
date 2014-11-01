package com.blackrook.engine;

import java.lang.reflect.Constructor;

import com.blackrook.commons.Common;
import com.blackrook.commons.Reflect;
import com.blackrook.commons.hash.Hash;
import com.blackrook.commons.hash.HashMap;
import com.blackrook.commons.list.List;
import com.blackrook.engine.annotation.Component;
import com.blackrook.engine.annotation.ComponentConstructor;
import com.blackrook.engine.components.EngineConsoleManager;
import com.blackrook.engine.exception.EngineSetupException;

/**
 * The main engine, created as the centerpoint of the communication between components
 * or as a main mediator between system components.
 * @author Matthew Tropiano
 */
public final class Engine
{
	/** Engine singleton map. */
	private HashMap<Class<?>, Object> engineSingletons;
	/** Engine console manager. */
	private EngineConsoleManager consoleManager;
	
	/**
	 * Creates the engine and all of the other stuff.
	 * @param config the configuration to use for engine setup.
	 */
	public Engine(EngineConfig config)
	{
		engineSingletons = new HashMap<Class<?>, Object>();
		consoleManager = createOrGetComponent(EngineConsoleManager.class);
		
		for (Class<?> componentClass : getComponentClasses(config))
		{
			// TODO: test for important class types here.
			consoleManager.addEntries(createOrGetComponent(componentClass));
		}
		
	}
	
	/**
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T> T createOrGetComponent(Class<T> clazz)
	{
		T instance = null;
		if ((instance = getComponent(clazz)) != null)
			return instance;
		
		boolean foundConstructor = false;
		for (Constructor<T> cons : (Constructor<T>[])clazz.getConstructors())
		{
			if (foundConstructor)
				break;
			
			if (!cons.isAnnotationPresent(ComponentConstructor.class))
				continue;
			
			foundConstructor = true;
			
			Class<?>[] types = cons.getParameterTypes();
			Object[] params = new Object[types.length]; 
			for (int i = 0; i < types.length; i++)
			{
				if (types[i].equals(clazz))
					throw new EngineSetupException("Circular dependency detected: class "+types[i].getSimpleName()+" is the same as this one: "+clazz.getSimpleName());
				params[i] = createOrGetComponent(types[i]);
			}
			
			instance = Reflect.construct(cons, params);
		}
		
		if (!foundConstructor)
			instance = Reflect.create(clazz);
		
		engineSingletons.put(clazz, instance);
		return instance;
	}
	
	/**
	 * Gets the engine-spawned singleton class assigned to the provided class.
	 * Returns null if not a valid component.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getComponent(Class<T> clazz)
	{
		return (T)engineSingletons.get(clazz);
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
		for (String className : Reflect.getClasses(config.getPackageRoot()))
			packageMap.put(className);
		
		for (String className : packageMap)
		{
			Class<?> clz = null;
			try {
				clz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("This should not have happened.", e);
			}
			
			Component ecomponent = clz.getAnnotation(Component.class);
			if (ecomponent == null)
				continue;
			
			outList.add(clz);
		}
		
		return outList;
	}
	
}
