package com.blackrook.engine;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import com.blackrook.archetext.ArcheTextIncluder;
import com.blackrook.archetext.ArcheTextReader;
import com.blackrook.archetext.ArcheTextRoot;
import com.blackrook.archetext.exception.ArcheTextParseException;
import com.blackrook.commons.Common;
import com.blackrook.commons.Reflect;
import com.blackrook.commons.hash.Hash;
import com.blackrook.commons.list.List;
import com.blackrook.engine.annotation.Element;
import com.blackrook.engine.annotation.ElementConstructor;
import com.blackrook.engine.annotation.resource.Resource;
import com.blackrook.engine.exception.EngineSetupException;
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
			public InputStream getIncludeResource(String streamName, String path) throws IOException
			{
				// convert backslash to slash.
				path = path.replace("\\", "/");
				
				String parentPath = null;
				int slashidx = streamName.lastIndexOf("/");
				if (slashidx >= 0)
					parentPath = streamName.substring(0, slashidx) + "/";
				else
					parentPath = "";
	
				FSFile nextfile = fileSystem.getFile(parentPath + path);
				if (nextfile != null)
					return nextfile.getInputStream();
				else
				{
					nextfile = fileSystem.getFile(path);
					if (nextfile != null)
						return nextfile.getInputStream();
					return null;
				}
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
				Common.close(in);
			}
		}
		
		return out;
	}

}
