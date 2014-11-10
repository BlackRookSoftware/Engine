package com.blackrook.engine;

import java.lang.reflect.Constructor;

import com.blackrook.commons.Common;
import com.blackrook.commons.Reflect;
import com.blackrook.commons.hash.Hash;
import com.blackrook.commons.hash.HashMap;
import com.blackrook.commons.list.List;
import com.blackrook.engine.annotation.EngineComponent;
import com.blackrook.engine.annotation.EngineComponentConstructor;
import com.blackrook.engine.annotation.EnginePooledComponent;
import com.blackrook.engine.components.EnginePoolable;
import com.blackrook.engine.console.EngineConsoleManager;
import com.blackrook.engine.exception.EnginePoolUnavailableException;
import com.blackrook.engine.exception.EngineSetupException;
import com.blackrook.engine.exception.NoSuchComponentException;

/**
 * The main engine, created as the centerpoint of the communication between components
 * or as a main mediator between system components.
 * @author Matthew Tropiano
 */
public final class Engine
{
	/** Engine singleton map. */
	private HashMap<Class<?>, Object> engineSingletons;
	/** Engine pooled object map. */
	private HashMap<Class<?>, EnginePool<EnginePoolable>> enginePools;
	
	/** Engine console manager. */
	private EngineConsoleManager consoleManager;
	
	/**
	 * Creates the engine and all of the other stuff.
	 * @param config the configuration to use for engine setup.
	 */
	@SuppressWarnings("unchecked")
	public Engine(EngineConfig config)
	{
		engineSingletons = new HashMap<Class<?>, Object>();
		enginePools = new HashMap<Class<?>, EnginePool<EnginePoolable>>();
		
		engineSingletons.put(Engine.class, this);
		
		consoleManager = createOrGetComponent(EngineConsoleManager.class);
		
		for (Class<?> componentClass : getComponentClasses(config))
		{
			if (componentClass.isAnnotationPresent(EnginePooledComponent.class))
			{
				if (!EnginePoolable.class.isAssignableFrom(componentClass))
					throw new EngineSetupException("Found EnginePooled annotation on a class that does not implement EnginePoolable.");
				
				Class<EnginePoolable> poolClass = (Class<EnginePoolable>)componentClass;
				EnginePooledComponent anno = componentClass.getAnnotation(EnginePooledComponent.class);
				enginePools.put(poolClass, new EnginePool<EnginePoolable>(this, poolClass, getAnnotatedConstructor(poolClass), anno.policy(), anno.value(), anno.expansion()));
			}
			else if (componentClass.isAnnotationPresent(EngineComponent.class))
			{
				Object object = createOrGetComponent(componentClass);
				consoleManager.addEntries(object);
			}
			
		}
		
	}
	
	/**
	 * Finds an returns this component's {@link EngineComponentConstructor}, if any.
	 * @param clazz the class to search.
	 * @return a viable constructor or null if no constructor annotated with {@link EngineComponentConstructor}.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Constructor<T> getComponentConstructor(Class<T> clazz)
	{
		Constructor<T> foundConstructor = null;
		for (Constructor<T> cons : (Constructor<T>[])clazz.getConstructors())
		{
			if (foundConstructor != null)
				return foundConstructor;
			
			if (!cons.isAnnotationPresent(EngineComponentConstructor.class))
				continue;
		}

		return null;
	}
	
	/**
	 * Creates a new component for a class and using one of its constructors.
	 * @param clazz the class to instantiate.
	 * @param constructor the constructor to call for instantiation.
	 * @return the new class instance.
	 */
	public <T> T createComponent(Class<T> clazz, Constructor<T> constructor)
	{
		if (constructor == null)
			return Reflect.create(clazz);
		
		Class<?>[] types = constructor.getParameterTypes();
		Object[] params = new Object[types.length]; 
		for (int i = 0; i < types.length; i++)
		{
			if (types[i].equals(clazz))
				throw new EngineSetupException("Circular dependency detected: class "+types[i].getSimpleName()+" is the same as this one: "+clazz.getSimpleName());
			params[i] = createOrGetComponent(types[i]);
		}
		return Reflect.construct(constructor, params);
	}

	/**
	 * Gets the engine-spawned singleton class assigned to the provided class.
	 * @throws NoSuchComponentException if the provided class is not a valid component.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getComponent(Class<T> clazz)
	{
		if (!engineSingletons.containsKey(clazz))
			throw new NoSuchComponentException("The class "+clazz.getSimpleName()+" is not a valid singleton component.");
		return (T)engineSingletons.get(clazz);
	}
	
	/**
	 * Gets the next available pooled component assigned to the provided class.
	 * @throws NoSuchComponentException if the provided class is not a valid pooled component.
	 * @throws EnginePoolUnavailableException if the pool's policy is to throw an exception if an object cannot be returned.
	 */
	@SuppressWarnings("unchecked")
	public <T extends EnginePoolable> T getPooledComponent(Class<T> clazz)
	{
		EnginePool<T> pool = (EnginePool<T>)enginePools.get(clazz);
		if (pool == null)
			throw new NoSuchComponentException("The class "+clazz.getSimpleName()+" is not a valid pooled component.");
		return (T)pool.getAvailable();
	}
	
	/**
	 * Creates or gets an engine singleton component by class.
	 * @param clazz the class to create/retrieve.
	 */
	@SuppressWarnings("unchecked")
	private <T> T createOrGetComponent(Class<T> clazz)	{
		if (engineSingletons.containsKey(clazz))
			return (T)engineSingletons.get(clazz);
		
		T instance = createComponent(clazz, getAnnotatedConstructor(clazz));
		engineSingletons.put(clazz, instance);
		return instance;
	}

	/**
	 * Returns the specific constructor to use for this class.
	 */
	@SuppressWarnings("unchecked")
	private <T> Constructor<T> getAnnotatedConstructor(Class<T> clazz)
	{
		for (Constructor<T> cons : (Constructor<T>[])clazz.getConstructors())
		{
			if (!cons.isAnnotationPresent(EngineComponentConstructor.class))
				continue;
			else
				return cons;
		}
		
		return null;
	}
	
	/**
	 * Adds engine singletons to the engine singleton manager.
	 * @param config the configuration to use for engine setup.
	 */
	private Iterable<Class<?>> getComponentClasses(EngineConfig config)
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
			
			if (isValidComponent(clz))
				outList.add(clz);
		}
		
		return outList;
	}
	
	private boolean isValidComponent(Class<?> clazz)
	{
		return
			clazz.isAnnotationPresent(EngineComponent.class)
			|| clazz.isAnnotationPresent(EnginePooledComponent.class)
			;
	}
	
}
