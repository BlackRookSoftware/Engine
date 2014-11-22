package com.blackrook.engine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import com.blackrook.archetext.ArcheTextIncluder;
import com.blackrook.archetext.ArcheTextObject;
import com.blackrook.archetext.ArcheTextReader;
import com.blackrook.archetext.ArcheTextRoot;
import com.blackrook.commons.Common;
import com.blackrook.commons.ObjectPair;
import com.blackrook.commons.Reflect;
import com.blackrook.commons.hash.Hash;
import com.blackrook.commons.hash.HashMap;
import com.blackrook.commons.linkedlist.Queue;
import com.blackrook.commons.list.List;
import com.blackrook.commons.logging.Logger;
import com.blackrook.commons.logging.LoggingDriver;
import com.blackrook.commons.logging.LoggingFactory;
import com.blackrook.commons.logging.LoggingFactory.LogLevel;
import com.blackrook.commons.logging.driver.ConsoleLogger;
import com.blackrook.commons.logging.driver.PrintStreamLogger;
import com.blackrook.engine.annotation.Component;
import com.blackrook.engine.annotation.ComponentConstructor;
import com.blackrook.engine.annotation.PooledComponent;
import com.blackrook.engine.annotation.ResourceComponent;
import com.blackrook.engine.exception.EngineSetupException;
import com.blackrook.engine.exception.NoSuchComponentException;
import com.blackrook.engine.roles.EngineDevice;
import com.blackrook.engine.roles.EngineListener;
import com.blackrook.engine.roles.EngineMessageListener;
import com.blackrook.engine.roles.EnginePoolable;
import com.blackrook.engine.roles.EngineResource;
import com.blackrook.engine.roles.EngineState;
import com.blackrook.engine.roles.EngineUpdatable;
import com.blackrook.engine.roles.EngineWindow;
import com.blackrook.engine.struct.EngineMessage;
import com.blackrook.fs.FSFile;

/**
 * The main engine, created as the centerpoint of the communication between components
 * or as a main mediator between system components.
 * TODO: Add updater stuff and state manager stuff.
 * @author Matthew Tropiano
 */
public final class Engine
{
	/** Logger. */
	private Logger logger;
	/** Engine logging factory. */
	private LoggingFactory loggingFactory;
	/** Engine filesystem. */
	private EngineFileSystem fileSystem;

	/** Engine singleton map. */
	private HashMap<Class<?>, Object> singletons;
	/** Engine pooled object map. */
	private HashMap<Class<?>, EnginePool<EnginePoolable>> pools;
	/** Engine devices. */
	private HashMap<String, EngineDevice> devices;
	/** Engine resources. */
	private HashMap<Class<?>, EngineResourceList<?>> resources;
	
	/** Engine listener. */
	private Queue<EngineListener> listeners;
	/** Engine message receiver. */
	private Queue<EngineMessageListener> messageListeners;
	/** Engine state manager. */
	private EngineStateManager stateManager;
	/** Engine update ticker. */
	private EngineTicker updateTicker;
	
	/** Engine console. */
	private EngineConsole console;
	/** Engine console manager. */
	private EngineConsoleManager consoleManager;
	
	/**
	 * Creates the engine and all of the other stuff.
	 * @param config the configuration to use for engine setup.
	 */
	@SuppressWarnings("unchecked")
	public Engine(EngineConfig config)
	{
		singletons = new HashMap<Class<?>, Object>();
		pools = new HashMap<Class<?>, EnginePool<EnginePoolable>>();
		devices = new HashMap<String, EngineDevice>();
		resources = new HashMap<Class<?>, EngineResourceList<?>>();
		listeners = new Queue<EngineListener>();
		messageListeners = new Queue<EngineMessageListener>();

		// set up logging.
		loggingFactory = new LoggingFactory();
		loggingFactory.addDriver(new ConsoleLogger());

		boolean debugMode = config.getDebugMode();
		singletons.put(Engine.class, this);
		singletons.put(EngineConfig.class, config); // uses base class.
		singletons.put(config.getClass(), config); // uses runtime class.

		logger = getLogger(Engine.class);

		consoleManager = createOrGetComponent(EngineConsoleManager.class, debugMode);
		console = createOrGetComponent(EngineConsole.class, debugMode); 

		stateManager = new EngineStateManager();
		updateTicker = new EngineTicker(this, config);
		
		PrintStream ps;
		try {
			FileOutputStream fos = new FileOutputStream(new File(config.getLogFilePath()));
			if (fos != null)
			{
				ps = new PrintStream(fos, true);
				PrintStreamLogger pslogger = new PrintStreamLogger(ps);
				loggingFactory.addDriver(pslogger);
			}
			else
			{
				console.println("ERROR: Could not open log file "+config.getLogFilePath());
			}
		} catch (IOException e) {
			console.println("ERROR: Could not open log file "+config.getLogFilePath());
		}
		
		
		loggingFactory.addDriver(new LoggingDriver()
		{
			final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			@Override
			public void log(Date time, LogLevel level, String source, String message, Throwable throwable)
			{
				console.printfln("%s <%s> %s: %s", DATE_FORMAT.format(time), source, level, message);
			}
		});

		fileSystem = new EngineFileSystem(this, config);
		singletons.put(EngineFileSystem.class, fileSystem);

		// load resource definitions.
		ArcheTextRoot resourceDefinitionRoot = loadResourceDefinitions(config.getResourceDefinitionFile());
		
		final EngineWindowEventReceiver windowEventReceiver = new EngineWindowEventReceiver()
		{
			@Override
			public void fireRestore()
			{
				for (EngineListener listener : listeners)
					listener.onRestore();
			}
			
			@Override
			public void fireMouseExit()
			{
				for (EngineListener listener : listeners)
					listener.onMouseExit();
			}
			
			@Override
			public void fireMouseEnter()
			{
				for (EngineListener listener : listeners)
					listener.onMouseEnter();
			}
			
			@Override
			public void fireMinimize()
			{
				for (EngineListener listener : listeners)
					listener.onMinimize();
			}
			
			@Override
			public void fireFocus()
			{
				for (EngineListener listener : listeners)
					listener.onFocus();
			}
			
			@Override
			public void fireClosing()
			{
				for (EngineListener listener : listeners)
					listener.onClosing();
			}
			
			@Override
			public void fireBlur()
			{
				for (EngineListener listener : listeners)
					listener.onBlur();
			}
		};
		
		logger.debug("Scanning classes...");
		for (Class<?> componentClass : getComponentClasses(config))
		{
			if (componentClass.isAnnotationPresent(ResourceComponent.class))
			{
				if (!EngineResource.class.isAssignableFrom(componentClass))
					throw new EngineSetupException("Found EngineResourceComponent annotation on a class that does not implement EngineResource.");
				
				Class<EngineResource> resourceClass = (Class<EngineResource>)componentClass;
				ResourceComponent anno = componentClass.getAnnotation(ResourceComponent.class);
				String className = resourceClass.getSimpleName();
				className = Character.toLowerCase(className.charAt(0)) + className.substring(1);
				String structName = Common.isEmpty(anno.value()) ? className : anno.value();

				EngineResourceList<EngineResource> resourceList = new EngineResourceList<EngineResource>(resourceClass); 
				resources.put(resourceClass, resourceList);
				
				int added = 0;
				for (ArcheTextObject object : resourceDefinitionRoot.getAllByType(structName))
				{
					resourceList.add(object.newObject(resourceClass));
					added++;
				}
				
				logger.infof("Created resource list. %s (count %d)", resourceClass.getSimpleName(), added);
			}
			else if (componentClass.isAnnotationPresent(PooledComponent.class))
			{
				if (!EnginePoolable.class.isAssignableFrom(componentClass))
					throw new EngineSetupException("Found EnginePooledComponent annotation on a class that does not implement EnginePoolable.");
				
				Class<EnginePoolable> poolClass = (Class<EnginePoolable>)componentClass;
				PooledComponent anno = componentClass.getAnnotation(PooledComponent.class);
				pools.put(poolClass, new EnginePool<EnginePoolable>(this, poolClass, getAnnotatedConstructor(poolClass), anno.policy(), anno.value(), anno.expansion()));
				logger.infof("Created pool. %s (count %d)", poolClass.getSimpleName(), anno.value());
			}
			else if (componentClass.isAnnotationPresent(Component.class))
			{
				Object obj = createOrGetComponent(componentClass, debugMode);
				if (obj instanceof EngineDevice)
					devices.put(((EngineDevice)obj).getName(), (EngineDevice)obj);
				if (obj instanceof EngineListener)
					listeners.enqueue((EngineListener)obj);
				if (obj instanceof EngineMessageListener)
					messageListeners.enqueue((EngineMessageListener)obj);
				if (obj instanceof EngineWindow)
					((EngineWindow)obj).addWindowEventReceiver(windowEventReceiver);

				if ((obj instanceof EngineUpdatable) && !(obj instanceof EngineState))
					/* Add to updater */;
			}
		}

		// Starts the devices.
		for (ObjectPair<?, EngineDevice> device : devices)
		{
			EngineDevice ed = device.getValue(); 
			logger.infof("Starting device %s.", ed.getName());
			if (ed.create())
				logger.infof("Finished starting device %s.", ed.getName());
			else
				logger.errorf("Failed starting device %s.", ed.getName());
		}
		
	}

	/**
	 * Gets the engine-spawned singleton class assigned to the provided class.
	 * @throws NoSuchComponentException if the provided class is not a valid component.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getComponent(Class<T> clazz)
	{
		if (!singletons.containsKey(clazz))
			throw new NoSuchComponentException("The class "+clazz.getSimpleName()+" is not a valid singleton component.");
		return (T)singletons.get(clazz);
	}
	
	/**
	 * Gets the pool assigned to the provided class.
	 * @throws NoSuchComponentException if the provided class is not a valid pooled component.
	 */
	@SuppressWarnings("unchecked")
	public <T extends EnginePoolable> EnginePool<T> getPool(Class<T> clazz)
	{
		EnginePool<T> pool = (EnginePool<T>)pools.get(clazz);
		if (pool == null)
			throw new NoSuchComponentException("The class "+clazz.getSimpleName()+" is not a valid pooled component.");
		return pool;
	}
	
	/**
	 * Returns the resource list that stores a set of resources.
	 * @param clazz the resource class to retrieve the list of.
	 * @throws NoSuchComponentException if the provided class is not a valid resource component.
	 */
	@SuppressWarnings("unchecked")
	public <T extends EngineResource> EngineResourceList<T> getResourceList(Class<T> clazz)
	{
		EngineResourceList<T> list = (EngineResourceList<T>)resources.get(clazz);
		if (list == null)
			throw new NoSuchComponentException("The class "+clazz.getSimpleName()+" is not a valid resource component.");
		return list;
	}
	
	/**
	 * Returns a logger instance for a particular component.
	 * @param name the component name to log things for.
	 * @return a logger to use.
	 */
	public Logger getLogger(String name)
	{
		return loggingFactory.getLogger(name);
	}
	
	/**
	 * Returns a logger instance for a particular component.
	 * @param clz the component name to log things for.
	 * @return a logger to use.
	 */
	public Logger getLogger(Class<?> clz)
	{
		return loggingFactory.getLogger(clz, false);
	}
	
	/**
	 * Broadcasts a message to all message listeners.
	 * @param message the message to broadcast to all who would listen.
	 */
	public void sendMessage(EngineMessage message)
	{
		for (EngineMessageListener listener : messageListeners)
			listener.onEngineMessage(message);
	}
	
	/**
	 * Restarts an engine device. 
	 * @param name the name of the device.
	 * @return true if successful, false if not.
	 */
	public boolean restartDevice(String name)
	{
		return destroyDevice(name) && createDevice(name);
	}

	/**
	 * Initiates engine shutdown.
	 * <p>All listeners have {@link EngineListener#onShutDown()} called on them, all
	 * devices have {@link EngineDevice#destroy()} called on them, and tells the JVM to exit.
	 */
	public void shutDown(int status)
	{
		logger.infof("Shutdown initiated.");
		for (EngineListener listener : listeners)
			listener.onShutDown();
		for (ObjectPair<?, EngineDevice> device : devices)
		{
			EngineDevice ed = device.getValue(); 
			logger.infof("Destroying device %s.", ed.getName());
			if (ed.destroy())
				logger.infof("Finished destroying device %s.", ed.getName());
			else
				logger.errorf("Failed destroying device %s.", ed.getName());
		}
		System.exit(status);
	}
	
	/**
	 * Creates a new component for a class and using one of its constructors.
	 * @param clazz the class to instantiate.
	 * @param constructor the constructor to call for instantiation.
	 * @return the new class instance.
	 */
	<T> T createComponent(Class<T> clazz, Constructor<T> constructor)
	{
		return createComponent(clazz, constructor, false);
	}

	/**
	 * Creates a new component for a class and using one of its constructors.
	 * @param clazz the class to instantiate.
	 * @param constructor the constructor to call for instantiation.
	 * @return the new class instance.
	 */
	<T> T createComponent(Class<T> clazz, Constructor<T> constructor, boolean debugMode)
	{
		T object = null;
		
		if (constructor == null)
			object = Reflect.create(clazz);
		else
		{
			Class<?>[] types = constructor.getParameterTypes();
			Object[] params = new Object[types.length]; 
			for (int i = 0; i < types.length; i++)
			{
				if (types[i].equals(clazz))
					throw new EngineSetupException("Circular dependency detected: class "+types[i].getSimpleName()+" is the same as this one: "+clazz.getSimpleName());
				else if (Logger.class.isAssignableFrom(types[i]))
					params[i] = getLogger(clazz);
				else
					params[i] = createOrGetComponent(types[i], debugMode);
			}
			
			object = Reflect.construct(constructor, params);
		}
	
		if (!clazz.isAnnotationPresent(Component.class))
			return object;
		
		consoleManager.addEntries(object, debugMode);
		logger.debugf("Created component. %s", clazz.getSimpleName());
		
		// check if device.
		if (EngineDevice.class.isAssignableFrom(clazz))
		{
			EngineDevice device = (EngineDevice)object;
			devices.put(device.getName(), device);
			logger.debugf("%s added to devices.", clazz.getSimpleName());
		}
	
		// check if message listener.
		if (EngineMessageListener.class.isAssignableFrom(clazz))
		{
			EngineMessageListener listener = (EngineMessageListener)object;
			messageListeners.add(listener);
			logger.debugf("%s added to message listeners.", clazz.getSimpleName());
		}
		
		return object;
	}

	/**
	 * Returns the names of all devices. 
	 */
	String[] getDeviceNames()
	{
		String[] out = new String[devices.size()];
		Iterator<String> it = devices.keyIterator();
		int i = 0;
		while (it.hasNext())
			out[i++] = it.next();
		return out;
	}
	
	/**
	 * Creates an engine device. 
	 * @param name the name of the device.
	 * @return true if successful, false if not.
	 */
	private boolean createDevice(String name)
	{
		EngineDevice device = devices.get(name);
		if (device != null)
		{
			if (device.isActive())
			{
				return false;
			}
			else
			{
				logger.infof("Created device %s.", device.getName());
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Creates an engine device. 
	 * @param name the name of the device.
	 * @return true if successful, false if not.
	 */
	private boolean destroyDevice(String name)
	{
		EngineDevice device = devices.get(name);
		if (device != null)
		{
			if (!device.isActive())
			{
				return false;
			}
			else
			{
				logger.infof("Destroyed device %s.", device.getName());
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Creates or gets an engine singleton component by class.
	 * @param clazz the class to create/retrieve.
	 */
	@SuppressWarnings("unchecked")
	private <T> T createOrGetComponent(Class<T> clazz, boolean debug)
	{
		if (singletons.containsKey(clazz))
			return (T)singletons.get(clazz);
		
		T instance = createComponent(clazz, getAnnotatedConstructor(clazz), debug);
		singletons.put(clazz, instance);
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
			if (!cons.isAnnotationPresent(ComponentConstructor.class))
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
		for (String className : Reflect.getClasses(Engine.class.getPackage().getName()+".components"))
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
			clazz.isAnnotationPresent(Component.class)
			|| clazz.isAnnotationPresent(PooledComponent.class)
			|| clazz.isAnnotationPresent(ResourceComponent.class)
			;
	}

	private ArcheTextRoot loadResourceDefinitions(String resourceDefinitionFile)
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
			} catch (IOException e) {
				throw new EngineSetupException(e);
			} finally {
				Common.close(in);
			}
		}
		
		return out;
	}
	
}
