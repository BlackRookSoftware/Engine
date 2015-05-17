package com.blackrook.engine;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import com.blackrook.commons.Reflect;
import com.blackrook.commons.hash.Hash;
import com.blackrook.commons.list.List;
import com.blackrook.engine.annotation.Element;
import com.blackrook.engine.annotation.ElementConstructor;
import com.blackrook.engine.annotation.resource.Resource;
import com.blackrook.engine.exception.EngineSetupException;

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
			if (cons.isAnnotationPresent(ElementConstructor.class))
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
	 * Adds engine singletons to the engine singleton manager.
	 * @param config the configuration to use for engine setup.
	 */
	static Iterable<Class<?>> getSingletonClasses(EngineConfig config)
	{
		List<Class<?>> outList = new List<Class<?>>();
		
		// Scan for singletons to instantiate.
		Hash<String> packageMap = new Hash<String>();
	
		for (String root : config.getPackageRoot())
			for (String className : Reflect.getClasses(root))
				packageMap.put(className);
		
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
			clazz.isAnnotationPresent(Element.class)
			|| clazz.isAnnotationPresent(Resource.class)
			;
	}

}
