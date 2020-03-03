/*******************************************************************************
 * Copyright (c) 2016-2020 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.blackrook.engine.annotation.EngineElement;
import com.blackrook.engine.annotation.EngineElementConstructor;
import com.blackrook.engine.exception.EngineSetupException;
import com.blackrook.engine.roles.EngineResourceGenerator;
import com.blackrook.engine.struct.Utils;

/**
 * Engine utility class.
 * @author Matthew Tropiano
 */
public final class EngineUtils
{
	private EngineUtils() {}
	
	/**
	 * Returns the specific constructor to use for element instantiation.
	 */
	@SuppressWarnings("unchecked")
	static <T> Constructor<T> getAnnotatedConstructor(Class<T> clazz)
	{
		Constructor<T> out = null;
		boolean hasDefaultConstructor = false;
		for (Constructor<T> cons : (Constructor<T>[])clazz.getConstructors())
		{
			if (cons.isAnnotationPresent(EngineElementConstructor.class))
			{
				if (out != null)
					throw new EngineSetupException("Found more than one constructor annotated with @ElementConstructor in class "+clazz.getName());
				else
					out = cons;
			}
			else if (cons.getParameterTypes().length == 0 && (cons.getModifiers() & Modifier.PUBLIC) != 0)
			{
				hasDefaultConstructor = true;
			}	
		}
	
		if (out == null && !hasDefaultConstructor)
		{
			throw new EngineSetupException("Class "+clazz.getName()+" has no viable constructors.");
		}
		
		return out;
	}

	/**
	 * Finds the resource and component classes to instantiate.
	 * @param config the engine configuration.
	 * @param outComponentClasses the output list of component classes.
	 * @param outResourceClasses the output list of resource classes.
	 * @param outResourceGeneratorClasses the output list of resource generator classes.
	 */
	@SuppressWarnings("unchecked")
	static void getComponentAndResourceClasses(
		EngineConfig config, 
		List<Class<?>> outComponentClasses, 
		List<Class<EngineResource>> outResourceClasses, 
		List<Class<EngineResourceGenerator>> outResourceGeneratorClasses
	) 
	{
		Set<String> componentStartupClass = new HashSet<>();
		if (!Utils.isEmpty(config.getStartupComponentClasses()))
			for (String name : config.getStartupComponentClasses())
				componentStartupClass.add(name);

		for (Class<?> componentClass : getSignificantClasses(config))
		{
			if (EngineResource.class.isAssignableFrom(componentClass))
			{
				outResourceClasses.add((Class<EngineResource>)componentClass);
			}
			else if (EngineResourceGenerator.class.isAssignableFrom(componentClass))
			{
				outResourceGeneratorClasses.add((Class<EngineResourceGenerator>)componentClass);
			}
			else if (componentClass.isAnnotationPresent(EngineElement.class))
			{
				EngineElement ecomp = componentClass.getAnnotation(EngineElement.class);
				if (config.getDebugMode() || (!config.getDebugMode() && !ecomp.debug()))
				{
					if (componentStartupClass.isEmpty() || componentStartupClass.contains(componentClass.getName()) || componentStartupClass.contains(componentClass.getSimpleName()))
					{
						outComponentClasses.add(componentClass);
					}
				}
			}
		}
	}
	
	/**
	 * Finds classes to be managed by the engine.
	 * @param config the configuration to use for engine setup.
	 */
	static Iterable<Class<?>> getSignificantClasses(EngineConfig config)
	{
		List<Class<?>> outList = new LinkedList<Class<?>>();
		
		// Scan for singletons to instantiate.
		Set<String> packageMap = new HashSet<String>();
	
		for (String root : config.getPackageRoots())
			for (String className : Utils.getClasses(root))
				packageMap.add(className);
		
		for (String className : packageMap)
		{
			Class<?> clz = null;
			try {
				clz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("This should not have happened.", e);
			}
			
			if (isValidSingleton(clz))
				outList.add(clz);
		}
		
		return outList;
	}

	static boolean isValidSingleton(Class<?> clazz)
	{
		return
			!Modifier.isAbstract(clazz.getModifiers())
			|| clazz.isAnnotationPresent(EngineElement.class)
			|| EngineResource.class.isAssignableFrom(clazz)
			|| EngineResourceGenerator.class.isAssignableFrom(clazz)
		;
	}

}
