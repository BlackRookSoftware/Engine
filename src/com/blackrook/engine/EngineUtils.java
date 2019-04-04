/*******************************************************************************
 * Copyright (c) 2016-2019 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import com.blackrook.archetext.ArcheTextIncluder;
import com.blackrook.archetext.ArcheTextReader;
import com.blackrook.archetext.ArcheTextRoot;
import com.blackrook.archetext.exception.ArcheTextParseException;
import com.blackrook.commons.Reflect;
import com.blackrook.commons.hash.Hash;
import com.blackrook.commons.list.List;
import com.blackrook.commons.util.IOUtils;
import com.blackrook.commons.util.ObjectUtils;
import com.blackrook.engine.annotation.EngineElement;
import com.blackrook.engine.annotation.EngineElementConstructor;
import com.blackrook.engine.exception.EngineSetupException;
import com.blackrook.engine.roles.EngineResource;
import com.blackrook.engine.roles.EngineResourceGenerator;
import com.blackrook.fs.FSFile;

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
		Hash<String> componentStartupClass = new Hash<>();
		if (!ObjectUtils.isEmpty(config.getStartupComponentClasses()))
			for (String name : config.getStartupComponentClasses())
				componentStartupClass.put(name);

		for (Class<?> componentClass : EngineUtils.getSignificantClasses(config))
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
		List<Class<?>> outList = new List<Class<?>>();
		
		// Scan for singletons to instantiate.
		Hash<String> packageMap = new Hash<String>();
	
		for (String root : config.getPackageRoots())
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
			!Modifier.isAbstract(clazz.getModifiers())
			|| clazz.isAnnotationPresent(EngineElement.class)
			|| EngineResource.class.isAssignableFrom(clazz)
			|| EngineResourceGenerator.class.isAssignableFrom(clazz)
		;
	}

	static ArcheTextRoot loadResourceDefinitions(final EngineFileSystem fileSystem, String resourceDefinitionFile)
	{
		FSFile[] resourceFiles;
		try {
			resourceFiles = fileSystem.getAllFileInstances(resourceDefinitionFile);
		} catch (IOException ex) {
			throw new EngineSetupException("Could not open resource file path instances: "+ ex.getLocalizedMessage());
		}
		
		ArcheTextRoot out = new ArcheTextRoot();
		ArcheTextIncluder includer = new ArcheTextIncluder()
		{
			@Override
			public String getIncludeResourceName(String streamName, String path) throws IOException 
			{
				// convert backslash to slash.
				path = path.replace("\\", "/");
				// convert backslash to slash.
				streamName = streamName.replace("\\", "/");
				
				String parentPath = null;
				int slashidx = streamName.lastIndexOf("/");
				if (slashidx >= 0)
					parentPath = streamName.substring(0, slashidx) + "/";
				else
					parentPath = "";
	
				FSFile nextfile = fileSystem.getFile(parentPath + path);
				if (nextfile != null)
					return parentPath + path;
				else
					return path;
			}

			@Override
			public InputStream getIncludeResource(String path) throws IOException
			{
				FSFile nextfile = fileSystem.getFile(path);
				if (nextfile != null)
					return nextfile.getInputStream();
				return null;
			}
		};
		
		
		for (int i = resourceFiles.length - 1; i >= 0; i--)
		{
			InputStream in = null;
			try {
				in = resourceFiles[i].getInputStream();
				ArcheTextReader.apply(resourceFiles[i].getPath(), in, includer, out);
			} catch (ArcheTextParseException e) {
				throw new EngineSetupException(e.getLocalizedMessage());
			} catch (IOException e) {
				throw new EngineSetupException(e);
			} finally {
				IOUtils.close(in);
			}
		}
		
		return out;
	}

}
