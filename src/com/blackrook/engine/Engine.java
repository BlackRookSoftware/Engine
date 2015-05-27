package com.blackrook.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

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
import com.blackrook.engine.annotation.Element;
import com.blackrook.engine.annotation.element.Ordering;
import com.blackrook.engine.annotation.resource.Resource;
import com.blackrook.engine.broadcaster.EngineInputBroadcaster;
import com.blackrook.engine.broadcaster.EngineWindowBroadcaster;
import com.blackrook.engine.exception.EngineSetupException;
import com.blackrook.engine.exception.NoSuchComponentException;
import com.blackrook.engine.resource.EngineResource;
import com.blackrook.engine.roles.EngineDevice;
import com.blackrook.engine.roles.EngineInputListener;
import com.blackrook.engine.roles.EngineShutdownListener;
import com.blackrook.engine.roles.EngineWindowListener;
import com.blackrook.engine.roles.EngineStartupListener;
import com.blackrook.engine.roles.EngineMessageListener;
import com.blackrook.engine.roles.EngineUpdateListener;
import com.blackrook.engine.struct.EngineMessage;
import com.blackrook.engine.struct.OrderedProperties;
import com.blackrook.engine.swing.ConsoleWindow;
import com.blackrook.fs.FSFile;

/**
 * The main engine, created as the centerpoint of the communication between components
 * or as a main mediator between system components.
 * @author Matthew Tropiano
 */
public final class Engine
{
	/** Logger. */
	private Logger logger;
	/** Engine logging factory. */
	private LoggingFactory loggingFactory;
	/** Engine config. */
	private EngineConfig config;
	/** Common window event receiver. */
	private EngineInputEventReceiver inputEventReceiver; 
	/** Common window event receiver. */
	private EngineWindowEventReceiver windowEventReceiver; 

	/** Engine singleton-in-construction set. */
	private Hash<Class<?>> singletonsConstructing;
	/** Engine singleton map. */
	private HashMap<Class<?>, Object> singletons;
	/** Engine devices. */
	private HashMap<String, EngineDevice> devices;
	/** Engine resources. */
	private HashMap<Class<?>, EngineResourceList<?>> resources;
	
	/** Ordering lists for startup. */
	private OrderingLists lists;
	/** Engine shutdown listener. */
	private Queue<EngineShutdownListener> shutdownListeners;
	/** Engine window listener. */
	private Queue<EngineWindowListener> windowListeners;
	/** Engine message listeners. */
	private Queue<EngineMessageListener> messageListeners;
	/** Engine input listeners. */
	private Queue<EngineInputListener> inputListeners;
	/** Engine update ticker. */
	private EngineTicker updateTicker;
	
	/** Engine console manager. */
	private EngineConsole console;
	/** File system. */
	private EngineFileSystem fileSystem;
	
	/**
	 * Creates the engine and all of the other stuff.
	 * @param config the configuration to use for engine setup.
	 */
	@SuppressWarnings("unchecked")
	public Engine(EngineConfig config)
	{
		this.config = config;
		
		singletonsConstructing = new Hash<Class<?>>();
		singletons = new HashMap<Class<?>, Object>();
		devices = new HashMap<String, EngineDevice>();
		resources = new HashMap<Class<?>, EngineResourceList<?>>();
		windowListeners = new Queue<EngineWindowListener>();
		shutdownListeners = new Queue<EngineShutdownListener>();
		messageListeners = new Queue<EngineMessageListener>();
		inputListeners = new Queue<EngineInputListener>();

		loggingFactory = new LoggingFactory();
		logger = loggingFactory.getLogger(Engine.class, false);

		loggingFactory.setLoggingLevel(config.getLogLevel() != null ? config.getLogLevel() : LogLevel.DEBUG);
		loggingFactory.addDriver(new ConsoleLogger());

		boolean debugMode = config.getDebugMode();
		
		singletons.put(Engine.class, this);
		singletons.put(EngineConfig.class, config); // uses base class.
		singletons.put(config.getClass(), config); // uses runtime class.

		// create console.
		console = new EngineConsole(this, config);
		console.addEntries(console, debugMode);
		singletons.put(EngineConsole.class, console);

		loggingFactory.addDriver(new LogDriver()
		{
			@Override
			public void output(String line)
			{
				console.getConsoleWindow().println(line);
			}
		});

		final PrintStream ps;
		try {
			FileOutputStream fos = new FileOutputStream(new File(config.getLogFile()));
			if (fos != null)
			{
				ps = new PrintStream(fos, true);
				loggingFactory.addDriver(new LogDriver()
				{
					@Override
					public void output(String line)
					{
						ps.println(line);
					}
				});
			}
			else
			{
				logger.error("ERROR: Could not open log file "+config.getLogFile());
			}
		} catch (IOException e) {
			logger.error("ERROR: Could not open log file "+config.getLogFile());
		}
		
		logger.info("Init application \"" + config.getApplicationName() + "\"");
		logger.info("Version " + config.getApplicationVersion());
		
		// create console manager.
		fileSystem = new EngineFileSystem(loggingFactory.getLogger(EngineFileSystem.class, false), this, config);
		singletons.put(EngineFileSystem.class, fileSystem);
		
		updateTicker = new EngineTicker(loggingFactory.getLogger(EngineTicker.class, false), this, config);
		singletons.put(EngineTicker.class, updateTicker);
				
		// load resource definitions.
		logger.infof("Opening resource definitions, %s", config.getResourceDefinitionFile());
		ArcheTextRoot resourceDefinitionRoot = loadResourceDefinitions(config.getResourceDefinitionFile());
		logger.info("Done.");
		
		inputEventReceiver = new EngineInputEventReceiver()
		{
			@Override
			public void fireInputFlag(int code, boolean set)
			{
				for (EngineInputListener listener : inputListeners)
					if (listener.onInputSet(code, set))
						break;
			}

			@Override
			public void fireInputValue(int code, double value)
			{
				for (EngineInputListener listener : inputListeners)
					if (listener.onInputValue(code, value))
						break;
			}
		};
		
		// create event receiver.
		windowEventReceiver = new EngineWindowEventReceiver()
		{
			@Override
			public void fireRestore()
			{
				for (EngineWindowListener listener : windowListeners)
					listener.onRestore();
			}
			
			@Override
			public void fireMouseExit()
			{
				for (EngineWindowListener listener : windowListeners)
					listener.onMouseExit();
			}
			
			@Override
			public void fireMouseEnter()
			{
				for (EngineWindowListener listener : windowListeners)
					listener.onMouseEnter();
			}
			
			@Override
			public void fireMinimize()
			{
				for (EngineWindowListener listener : windowListeners)
					listener.onMinimize();
			}
			
			@Override
			public void fireFocus()
			{
				for (EngineWindowListener listener : windowListeners)
					listener.onFocus();
			}
			
			@Override
			public void fireClosing()
			{
				for (EngineWindowListener listener : windowListeners)
					listener.onClosing();
			}
			
			@Override
			public void fireBlur()
			{
				for (EngineWindowListener listener : windowListeners)
					listener.onBlur();
			}
		};
		
		Queue<EngineStartupListener> starterComponents = new Queue<EngineStartupListener>();
		lists = new OrderingLists();
		Hash<String> componentStartupClass = new Hash<>();
		if (!Common.isEmpty(config.getStartupComponentClasses()))
			for (String name : config.getStartupComponentClasses())
				componentStartupClass.put(name);
		
		logger.debug("Scanning classes...");
		
		List<Class<EngineResource>> resourceClasses = new List<Class<EngineResource>>();
		List<Class<?>> componentClasses = new List<Class<?>>();
		
		for (Class<?> componentClass : EngineUtils.getSingletonClasses(config))
		{
			if (componentClass.isAnnotationPresent(Resource.class))
			{
				if (!EngineResource.class.isAssignableFrom(componentClass))
					throw new EngineSetupException("Found @Resource annotation on a class that does not implement EngineResource.");
				resourceClasses.add((Class<EngineResource>)componentClass);
			}
			else if (componentClass.isAnnotationPresent(Element.class))
			{
				Element ecomp = componentClass.getAnnotation(Element.class);
				if (config.getDebugMode() || (!config.getDebugMode() && !ecomp.debug()))
				{
					if (componentStartupClass.isEmpty() || componentStartupClass.contains(componentClass.getName()) || componentStartupClass.contains(componentClass.getSimpleName()))
					{
						componentClasses.add(componentClass);
					}
				}
			}
		}
		
		// create resources first.
		for (Class<EngineResource> clazz : resourceClasses)
		{
			Resource anno = clazz.getAnnotation(Resource.class);
			String className = clazz.getSimpleName();
			className = Character.toLowerCase(className.charAt(0)) + className.substring(1);
			String structName = Common.isEmpty(anno.value()) ? className : anno.value();

			EngineResourceList<EngineResource> resourceList = new EngineResourceList<EngineResource>(clazz); 
			resources.put(clazz, resourceList);
			
			int added = 0;
			for (ArcheTextObject object : resourceDefinitionRoot.getAllByType(structName))
			{
				resourceList.add(object.newObject(clazz));
				added++;
			}
			
			logger.infof("Created resource list. %s (count %d)", clazz.getSimpleName(), added);
		}
		
		// create components.
		for (Class<?> clazz : componentClasses)
		{
			createOrGetElement(clazz, debugMode);
		}
		
		/* Sort and add role singletons. */

		lists.sort();
		
		for (OrderingNode<EngineDevice> obj : lists.devices)
		{
			devices.put(obj.object.getDeviceName(), obj.object);
			logger.debugf("%s added to devices.", obj.object.getClass().getSimpleName());
		}
		
		for (OrderingNode<EngineWindowListener> obj : lists.windowListeners)
		{
			windowListeners.enqueue(obj.object);
			logger.debugf("%s added to window listeners.", obj.object.getClass().getSimpleName());
		}

		for (OrderingNode<EngineInputListener> obj : lists.inputListeners)
		{
			inputListeners.enqueue(obj.object);
			logger.debugf("%s added to input listeners.", obj.object.getClass().getSimpleName());
		}
		
		for (OrderingNode<EngineMessageListener> obj : lists.messageListeners)
		{
			messageListeners.enqueue(obj.object);
			logger.debugf("%s added to message listeners.", obj.object.getClass().getSimpleName());
		}
		
		for (OrderingNode<EngineStartupListener> obj : lists.startupListeners)
		{
			starterComponents.enqueue(obj.object);
			logger.debugf("%s added to startup listeners.", obj.object.getClass().getSimpleName());
		}

		for (OrderingNode<EngineShutdownListener> obj : lists.shutdownListeners)
		{
			shutdownListeners.enqueue(obj.object);
			logger.debugf("%s added to shutdown listeners.", obj.object.getClass().getSimpleName());
		}

		for (OrderingNode<EngineUpdateListener> obj : lists.updateListeners)
		{
			updateTicker.add(obj.object);
			logger.debugf("%s added to update listeners.", obj.object.getClass().getSimpleName());
		}
		
		loadGlobalVariables();
		loadUserVariables();
		
		/* Invoke and call. */

		// Starts the devices.
		for (ObjectPair<?, EngineDevice> device : devices)
		{
			EngineDevice ed = device.getValue(); 
			logger.infof("Starting device %s.", ed.getDeviceName());
			if (ed.create())
				logger.infof("Finished starting device %s.", ed.getDeviceName());
			else
				logger.errorf("Failed starting device %s.", ed.getDeviceName());
		}

		// call console commands.
		if (!Common.isEmpty(config.getConsoleCommandsToExecute()))
		{
			logger.info("Calling queued console commands...");
			for (String command : config.getConsoleCommandsToExecute())
				console.parseCommand(command);
		}
		
		// invokes main methods.
		logger.info("Invoking engine start methods.");
		// invoke start on stuff.
		while (!starterComponents.isEmpty())
			starterComponents.dequeue().onEngineStartup();
		
		// start ticker.
		updateTicker.start();
		logger.info("Started update ticker.");
		
		if (debugMode)
			openConsole();
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
	 * Opens the console.
	 */
	public void openConsole()
	{
		ConsoleWindow window = console.getConsoleWindow();
		if (!window.isVisible())
		{
			window.pack();
			window.setVisible(true);
		}
	}
	
	private void stopTicker()
	{
		logger.infof("Stopping ticker...");
		updateTicker.stop();
	}

	private void destroyAllDevices()
	{
		// destroy devices
		for (ObjectPair<?, EngineDevice> device : devices)
		{
			EngineDevice ed = device.getValue(); 
			logger.infof("Destroying device %s.", ed.getDeviceName());
			if (ed.destroy())
				logger.infof("Finished destroying device %s.", ed.getDeviceName());
			else
				logger.errorf("Failed destroying device %s.", ed.getDeviceName());
		}
	}

	private void loadGlobalVariables()
	{
		Properties settings = null;
		InputStream inStream = null;
		Object settingValue = null;

		if (config.getGlobalVariablesFile() != null)
		{
			logger.infof("Loading global settings...");
			settings = new Properties();
			try {
				inStream = fileSystem.openGlobalSettingFile(config.getGlobalVariablesFile());
				settings = new Properties();
				settings.load(inStream);
			} catch (FileNotFoundException e) {
				logger.infof("Could not open global settings from file \"%s\". Doesn't exist.", fileSystem.getGlobalSettingFilePath(config.getGlobalVariablesFile()));
			} catch (IOException e) {
				logger.errorf(e, "Could not read global settings from file \"%s\".", fileSystem.getGlobalSettingFilePath(config.getGlobalVariablesFile()));
			} finally {
				Common.close(inStream);
			}
			for (String var : console.getVariableNames(true, true))
			{
				if ((settingValue = settings.getProperty(var)) != null)
					console.setVariable(var, settingValue);
			}
		}
	}
	
	private void loadUserVariables()
	{
		Properties settings = null;
		InputStream inStream = null;
		Object settingValue = null;

		if (config.getUserVariablesFile() != null)
		{
			logger.infof("Loading user settings...");
			settings = new Properties();
			try {
				inStream = fileSystem.openUserSettingFile(config.getUserVariablesFile());
				settings = new Properties();
				settings.load(inStream);
			} catch (FileNotFoundException e) {
				logger.infof("Could not open user settings from file \"%s\". Doesn't exist.", fileSystem.getUserSettingFilePath(config.getUserVariablesFile()));
			} catch (IOException e) {
				logger.errorf(e, "Could not read user settings from file \"%s\".", fileSystem.getUserSettingFilePath(config.getUserVariablesFile()));
			} finally {
				Common.close(inStream);
			}
			for (String var : console.getVariableNames(true, false))
			{
				if ((settingValue = settings.getProperty(var)) != null)
					console.setVariable(var, settingValue);
			}
		}

	}
	
	private void saveGlobalVariables()
	{
		String applicationString = config.getApplicationName() + " v" + config.getApplicationVersion();
		OrderedProperties settings = null;
		OutputStream outStream = null;
		if (config.getGlobalVariablesFile() != null)
		{
			logger.infof("Saving global settings...");
			settings = new OrderedProperties();
			for (String var : console.getVariableNames(true, true))
				settings.setProperty(var, console.getVariable(var, String.class));
			try {
				outStream = fileSystem.createGlobalSettingFile(config.getGlobalVariablesFile());
				settings.store(outStream, "Global settings for " + applicationString);
			} catch (IOException e) {
				logger.errorf(e, "Could not write global settings to file \"%s\".", fileSystem.getGlobalSettingFilePath(config.getGlobalVariablesFile()));
			} finally {
				Common.close(outStream);
			}
		}
	}
	
	private void saveUserVariables()
	{
		String applicationString = config.getApplicationName() + " v" + config.getApplicationVersion();
		OrderedProperties settings = null;
		OutputStream outStream = null;

		if (config.getUserVariablesFile() != null)
		{
			logger.infof("Saving user settings...");
			settings = new OrderedProperties();
			for (String var : console.getVariableNames(true, false))
				settings.setProperty(var, String.valueOf(console.getVariable(var)));
			try {
				outStream = fileSystem.createUserSettingFile(config.getUserVariablesFile());
				settings.store(outStream, "User settings for " + applicationString);
			} catch (IOException e) {
				logger.errorf(e, "Could not write user settings to file \"%s\".", fileSystem.getUserSettingFilePath(config.getUserVariablesFile()));
			} finally {
				Common.close(outStream);
			}
		}
	}

	/**
	 * Handles an uncaught, fatal exception and initiates engine shutdown.
	 * <p>The ticker is stopped, all devices have {@link EngineDevice#destroy()} called on them, 
	 * all listeners have {@link EngineShutdownListener#onUnexpectedEngineShutDown(Throwable)} called on them, and tells the JVM to exit.
	 */
	public void handleException(Throwable t)
	{
		logger.severe(t, "Uncaught exception thrown: " + t.getClass().getName() +": " + t.getLocalizedMessage());

		stopTicker();
		
		destroyAllDevices();
		
		logger.infof("Notifying listeners...");
		for (EngineShutdownListener listener : shutdownListeners)
			listener.onUnexpectedEngineShutDown(t);

		logger.infof("Shutting down JVM.");
		System.exit(-1);
	}

	/**
	 * Initiates engine shutdown.
	 * <p>The ticker is stopped, all listeners have {@link EngineShutdownListener#onEngineShutdown()} called on them, all settings are saved, 
	 * all devices have {@link EngineDevice#destroy()} called on them, and tells the JVM to exit.
	 * <p>Convenience method for <code>shutDown(0);</code>
	 * @see #shutDown(int)
	 */
	public void shutDown()
	{
		shutDown(0);
	}
	
	/**
	 * Initiates engine shutdown.
	 * <p>The ticker is stopped, all listeners have {@link EngineShutdownListener#onEngineShutdown()} called on them, all settings are saved, 
	 * all devices have {@link EngineDevice#destroy()} called on them, and tells the JVM to exit.
	 * @param status the status code to return upon program completion (aka "errorlevel" on some OSes).
	 */
	public void shutDown(int status)
	{
		logger.infof("Shutdown initiated.");

		stopTicker();
		saveUserVariables();
		saveGlobalVariables();

		logger.infof("Notifying listeners...");
		for (EngineShutdownListener listener : shutdownListeners)
			listener.onEngineShutdown();

		destroyAllDevices();
		
		logger.infof("Shutting down JVM. Bye!");
		System.exit(status);
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
	 * Creates a new component for a class and using one of its constructors.
	 * @param clazz the class to instantiate.
	 * @param constructor the constructor to call for instantiation.
	 * @param skipConsole if true, skips the CVAR and CCMD steps.
	 * @param debugMode if true, processes CVARs and CCMDs only available in debug mode.
	 * @return the new class instance.
	 */
	<T> T createElement(Class<T> clazz, Constructor<T> constructor, boolean debugMode)
	{
		T object = null;
		
		if (constructor == null)
		{
			object = Reflect.create(clazz);
		}
		else
		{
			singletonsConstructing.put(clazz);
			
			Class<?>[] types = constructor.getParameterTypes();
			Object[] params = new Object[types.length]; 
			for (int i = 0; i < types.length; i++)
			{
				if (singletonsConstructing.contains(types[i]))
					throw new EngineSetupException("Circular dependency detected in class "+clazz.getSimpleName()+": "+types[i].getSimpleName()+" has not finished constructing.");
				else if (Logger.class.isAssignableFrom(types[i]))
					params[i] = loggingFactory.getLogger(clazz);
				else
					params[i] = createOrGetElement(types[i], debugMode);
			}
			
			object = Reflect.construct(constructor, params);
			
			singletonsConstructing.remove(clazz);
		}
	
		if (!clazz.isAnnotationPresent(Element.class))
			return object;
		
		console.addEntries(object, debugMode);
		
		// check if engine window.
		if (EngineWindowBroadcaster.class.isAssignableFrom(clazz))
		{
			EngineWindowBroadcaster obj = (EngineWindowBroadcaster)object;
			obj.addWindowEventReceiver(windowEventReceiver);
			logger.debugf("%s was passed a window event receiver.", clazz.getSimpleName());
		}
				
		// check if engine input.
		if (EngineInputBroadcaster.class.isAssignableFrom(clazz))
		{
			EngineInputBroadcaster obj = (EngineInputBroadcaster)object;
			obj.addInputReceiver(inputEventReceiver);
			logger.debugf("%s was passed an input event receiver.", clazz.getSimpleName());
		}
		
		/* The listeners. */
		
		Ordering anno = clazz.getAnnotation(Ordering.class);
		int ordering = anno == null ? 0 : anno.value();
		
		// check if device.
		if (EngineDevice.class.isAssignableFrom(clazz))
		{
			EngineDevice obj = (EngineDevice)object;
			lists.devices.add(new OrderingNode<EngineDevice>(ordering, obj));
		}
	
		// check if engine listener.
		if (EngineWindowListener.class.isAssignableFrom(clazz))
		{
			EngineWindowListener obj = (EngineWindowListener)object;
			lists.windowListeners.add(new OrderingNode<EngineWindowListener>(ordering, obj));
		}
	
		// check if message listener.
		if (EngineMessageListener.class.isAssignableFrom(clazz))
		{
			EngineMessageListener obj = (EngineMessageListener)object;
			lists.messageListeners.add(new OrderingNode<EngineMessageListener>(ordering, obj));
		}
	
		// check if input listener.
		if (EngineInputListener.class.isAssignableFrom(clazz))
		{
			EngineInputListener obj = (EngineInputListener)object;
			lists.inputListeners.add(new OrderingNode<EngineInputListener>(ordering, obj));
		}
		
		// check if input listener.
		if (EngineShutdownListener.class.isAssignableFrom(clazz))
		{
			EngineShutdownListener obj = (EngineShutdownListener)object;
			lists.shutdownListeners.add(new OrderingNode<EngineShutdownListener>(ordering, obj));
		}
		
		// check if engine starter.
		if (EngineStartupListener.class.isAssignableFrom(clazz))
		{
			EngineStartupListener obj = (EngineStartupListener)object;
			lists.startupListeners.add(new OrderingNode<EngineStartupListener>(ordering, obj));
		}
	
		// check if update listener.
		if (EngineUpdateListener.class.isAssignableFrom(clazz))
		{
			EngineUpdateListener obj = (EngineUpdateListener)object;
			lists.updateListeners.add(new OrderingNode<EngineUpdateListener>(ordering, obj));
		}
	
		return object;
	}

	/**
	 * Creates or gets an engine singleton component by class.
	 * @param clazz the class to create/retrieve.
	 */
	@SuppressWarnings("unchecked")
	private <T> T createOrGetElement(Class<T> clazz, boolean debug)
	{
		if (singletons.containsKey(clazz))
			return (T)singletons.get(clazz);
		
		T instance = createElement(clazz, EngineUtils.getAnnotatedConstructor(clazz), debug);
		singletons.put(clazz, instance);
		logger.infof("Created element. %s", clazz.getSimpleName());
		return instance;
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
				logger.infof("Created device %s.", device.getDeviceName());
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
				logger.infof("Destroyed device %s.", device.getDeviceName());
				return true;
			}
		}
		return false;
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

	/**
	 * Common logging driver.
	 */
	private static abstract class LogDriver implements LoggingDriver
	{
		final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");
		

		@Override
		public void log(Date time, LogLevel level, String source, String message, Throwable throwable)
		{
			output(String.format("[%s] <%s> %s: %s", DATE_FORMAT.format(time), source, level, message));
			if (throwable != null)
			{
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				throwable.printStackTrace(pw);
				pw.flush();
				pw.close();
				Common.close(sw);
				output(sw.toString());
			}
		}
		
		/** 
		 * Outputs text to something. 
		 */
		protected abstract void output(String line);
	}
	
	/** Node for ordering lists of components. */
	private static class OrderingLists
	{
		private List<OrderingNode<EngineDevice>> devices;
		private List<OrderingNode<EngineWindowListener>> windowListeners;
		private List<OrderingNode<EngineInputListener>> inputListeners;
		private List<OrderingNode<EngineMessageListener>> messageListeners;
		private List<OrderingNode<EngineStartupListener>> startupListeners;
		private List<OrderingNode<EngineShutdownListener>> shutdownListeners;
		private List<OrderingNode<EngineUpdateListener>> updateListeners;
		
		private OrderingLists()
		{
			devices = new List<Engine.OrderingNode<EngineDevice>>();
			windowListeners = new List<Engine.OrderingNode<EngineWindowListener>>();
			inputListeners = new List<Engine.OrderingNode<EngineInputListener>>();
			messageListeners = new List<Engine.OrderingNode<EngineMessageListener>>();
			startupListeners = new List<Engine.OrderingNode<EngineStartupListener>>();
			shutdownListeners = new List<Engine.OrderingNode<EngineShutdownListener>>();
			updateListeners = new List<Engine.OrderingNode<EngineUpdateListener>>();
		}
		
		private void sort()
		{
			devices.sort();
			windowListeners.sort();
			inputListeners.sort();
			messageListeners.sort();
			startupListeners.sort();
			shutdownListeners.sort();
			updateListeners.sort();
		}
		
	}
	
	/** Node for ordering components. */
	private static class OrderingNode<X extends Object> implements Comparable<OrderingNode<?>>
	{
		private int ordering;
		private X object;
		
		private OrderingNode(int ordering, X object)
		{
			this.ordering = ordering;
			this.object = object;
		}

		@Override
		public int compareTo(OrderingNode<?> o)
		{
			return ordering - o.ordering;
		}
	}
	
}
