/*******************************************************************************
 * Copyright (c) 2016 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
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
import java.util.Properties;

import com.blackrook.archetext.ArcheTextObject;
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
import com.blackrook.engine.EngineResources.ResourceSet;
import com.blackrook.engine.annotation.EngineElement;
import com.blackrook.engine.annotation.element.Ordering;
import com.blackrook.engine.annotation.resource.DefinitionName;
import com.blackrook.engine.broadcaster.EngineInputBroadcaster;
import com.blackrook.engine.broadcaster.EngineInputEventReceiver;
import com.blackrook.engine.broadcaster.EngineMessageBroadcaster;
import com.blackrook.engine.broadcaster.EngineMessageReceiver;
import com.blackrook.engine.broadcaster.EngineWindowBroadcaster;
import com.blackrook.engine.broadcaster.EngineWindowEventReceiver;
import com.blackrook.engine.exception.EngineSetupException;
import com.blackrook.engine.roles.EngineDevice;
import com.blackrook.engine.roles.EngineInputListener;
import com.blackrook.engine.roles.EngineShutdownListener;
import com.blackrook.engine.roles.EngineWindowListener;
import com.blackrook.engine.roles.EngineReadyListener;
import com.blackrook.engine.roles.EngineMessageListener;
import com.blackrook.engine.roles.EngineResource;
import com.blackrook.engine.roles.EngineResourceGenerator;
import com.blackrook.engine.roles.EngineSettingsListener;
import com.blackrook.engine.roles.EngineUpdateListener;
import com.blackrook.engine.struct.OrderedProperties;
import com.blackrook.engine.swing.ConsoleWindow;

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
	/** Common message receiver. */
	private EngineMessageReceiver messageReceiver; 
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
	
	/** Engine settings listener. */
	private Queue<EngineSettingsListener> settingsListeners;
	/** Engine startup listener. */
	private Queue<EngineReadyListener> readyListeners;
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
	
	/**
	 * Creates the Engine and prepares all of its singletons and starts stuff up and returns the instance.
	 * @param config the engine configuration to use.
	 * @return the new Engine instance.
	 */
	public static Engine createEngine(EngineConfig config)
	{
		Engine out = new Engine(config);
		boolean debugMode = config.getDebugMode();

		createEngineLoggers(out, config);
		
		// create file system.
		EngineFileSystem fileSystem = new EngineFileSystem(out.loggingFactory.getLogger(EngineFileSystem.class, false), out, config);
		out.singletons.put(EngineFileSystem.class, fileSystem);

		// load resource definitions.
		ArcheTextRoot resourceDefinitionRoot;
		if (!Common.isEmpty(config.getResourceDefinitionFile()))
		{
			out.logger.infof("Opening resource definitions, %s", config.getResourceDefinitionFile());
			out.logger.info("Reading definitions...");
			resourceDefinitionRoot = EngineUtils.loadResourceDefinitions(fileSystem, config.getResourceDefinitionFile());
		}
		else
		{
			resourceDefinitionRoot = new ArcheTextRoot();
		}
		
		// Scan important classes.
		out.logger.debug("Scanning classes...");
		List<Class<?>> componentClasses = new List<Class<?>>();
		List<Class<EngineResource>> resourceClasses = new List<Class<EngineResource>>();
		List<Class<EngineResourceGenerator>> resourceGeneratorClasses = new List<Class<EngineResourceGenerator>>();
		EngineUtils.getComponentAndResourceClasses(config, componentClasses, resourceClasses, resourceGeneratorClasses);
	
		// Sort the resource generator classes.
		List<OrderingNode<EngineResourceGenerator>> generators = new List<>(resourceGeneratorClasses.size());
		for (Class<EngineResourceGenerator> clazz : resourceGeneratorClasses)
		{
			Ordering anno = clazz.getAnnotation(Ordering.class);
			int ordering = anno == null ? 0 : anno.value();
			try {
				generators.add(new OrderingNode<EngineResourceGenerator>(ordering, Reflect.create(clazz)));
			} catch (Exception e) {
				throw new EngineSetupException(e);
			}
		}
		generators.sort();

		// Create resources.
		out.logger.debug("Gathering/creating resources...");
		EngineResources resources = new EngineResources();
		out.singletons.put(EngineResources.class, resources);

		// Call generators first.
		for (OrderingNode<EngineResourceGenerator> generatorNode : generators)
		{
			EngineResourceGenerator generator = generatorNode.object;
			out.logger.debugf("Calling generator class %s...", generator.getClass().getSimpleName());
			try {
				generator.createResources(out.loggingFactory.getLogger(generator.getClass()), fileSystem, resources);
			} catch (EngineSetupException e) {
				throw e;
			} catch (Exception e) {
				throw new EngineSetupException("An error occurred during resource generation.", e);
			}
		}

		// Get/create resources from Archetext.
		for (Class<EngineResource> clazz : resourceClasses)
		{
			DefinitionName anno = clazz.getAnnotation(DefinitionName.class);
			String className = clazz.getSimpleName();
			className = Character.toLowerCase(className.charAt(0)) + className.substring(1);
			String structName = anno != null && !Common.isEmpty(anno.value()) ? anno.value() : null;
	
			if (structName != null) for (ArcheTextObject object : resourceDefinitionRoot.getAllByType(structName))
			{
				String name = object.getName();
				try {
					EngineResource resource;
					if ((resource = resources.getResource(clazz, name)) != null)
						object.applyToObject(clazz.cast(resource));
					else
						resources.addResource(object.newObject(clazz));
				} catch (Exception ex) {
					throw new EngineSetupException("Class "+clazz.getSimpleName()+" using ["+object.getType()+":"+name+"]: Could not create resource object.", ex);
				}
			}
		}
		
		out.createComponents(componentClasses, debugMode);
		
		out.loadGlobalVariables(fileSystem);
		out.loadUserVariables(fileSystem);
		
		// Starts the devices.
		out.createAllDevices();
	
		// call console commands.
		if (!Common.isEmpty(config.getConsoleCommandsToExecute()))
		{
			out.logger.info("Calling queued console commands...");
			for (String command : config.getConsoleCommandsToExecute())
				out.console.parseCommand(command);
		}
		
		// invokes main methods.
		out.logger.info("Invoking engine start methods.");
		// invoke start on stuff.
		for (EngineReadyListener listener : out.readyListeners)
			listener.onEngineReady();
		
		// start ticker.
		out.updateTicker.start();
		out.logger.info("Started update ticker.");
		
		return out;
	}

	/**
	 * Creates the engine loggers.
	 * @param config the engine configuration.
	 * @param engine the engine to apply to.
	 */
	private static void createEngineLoggers(final Engine engine, EngineConfig config)
	{
		engine.loggingFactory.setLoggingLevel(config.getLogLevel() != null ? config.getLogLevel() : LogLevel.DEBUG);
	
		engine.loggingFactory.addDriver(new LogDriver()
		{
			@Override
			public void output(String line)
			{
				System.out.println(line);
			}
		});
	
		engine.loggingFactory.addDriver(new LogDriver()
		{
			@Override
			public void output(String line)
			{
				engine.console.getConsoleWindow().println(line);
			}
		});
	
		if (!Common.isEmpty(config.getLogFile()))
		{
			final PrintStream ps;
			try {
				FileOutputStream fos = new FileOutputStream(new File(config.getLogFile()));
				ps = new PrintStream(fos, true);
				engine.loggingFactory.addDriver(new LogDriver()
				{
					@Override
					public void output(String line)
					{
						ps.println(line);
					}
				});
			} catch (IOException e) {
				engine.logger.error("ERROR: Could not open log file "+config.getLogFile());
			}
		}
		
		if (!Common.isEmpty(config.getApplicationName()))
			engine.logger.info("Init application \"" + config.getApplicationName() + "\"");
		else
			engine.logger.info("Init application.");
			
		if (!Common.isEmpty(config.getApplicationVersion()))
			engine.logger.info("Version " + config.getApplicationVersion());
	}

	/**
	 * Creates the engine and all of the other stuff.
	 * @param config the configuration to use for engine setup.
	 */
	private Engine(EngineConfig config)
	{
		this.config = config;
		
		singletonsConstructing = new Hash<Class<?>>();
		singletons = new HashMap<Class<?>, Object>();
		devices = new HashMap<String, EngineDevice>();
		windowListeners = new Queue<EngineWindowListener>();
		settingsListeners = new Queue<EngineSettingsListener>();
		readyListeners = new Queue<EngineReadyListener>(); 
		shutdownListeners = new Queue<EngineShutdownListener>();
		messageListeners = new Queue<EngineMessageListener>();
		inputListeners = new Queue<EngineInputListener>();

		loggingFactory = new LoggingFactory();
		logger = loggingFactory.getLogger(Engine.class, false);

		// Create message receiver.
		messageReceiver = new EngineMessageReceiver()
		{
			@Override
			public void sendMessage(Object type, Object... arguments)
			{
				for (EngineMessageListener listener : messageListeners)
					listener.onEngineMessage(type, arguments);
			}
		};
		
		// Create input receiver.
		inputEventReceiver = new EngineInputEventReceiver()
		{
			@Override
			public void fireInputFlag(String code, boolean set)
			{
				for (EngineInputListener listener : inputListeners)
					if (listener.onInputSet(code, set))
						break;
			}

			@Override
			public void fireInputValue(String code, double value)
			{
				for (EngineInputListener listener : inputListeners)
					if (listener.onInputValue(code, value))
						break;
			}
		};
		
		// Create event receiver.
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
			public void fireMouseMove(int canvasX, int canvasY)
			{
				for (EngineWindowListener listener : windowListeners)
					listener.onMouseMove(canvasX, canvasY);
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

			@Override
			public void fireResize(int width, int height)
			{
				for (EngineWindowListener listener : windowListeners)
					listener.onResize(width, height);
			}

		};

		updateTicker = new EngineTicker(loggingFactory.getLogger(EngineTicker.class, false), this, config);
				
		singletons.put(Engine.class, this);
		singletons.put(EngineConfig.class, config); // uses base class.
		singletons.put(config.getClass(), config); // uses runtime class.

		// create console.
		console = new EngineConsole(this, config);
		console.addEntries(console, config.getDebugMode());
		singletons.put(EngineConsole.class, console);
		
		singletons.put(EngineTicker.class, updateTicker);
	}

	private void createComponents(List<Class<?>> componentClasses, boolean debugMode)
	{
		OrderingLists lists = new OrderingLists();
		
		// create components.
		for (Class<?> clazz : componentClasses)
		{
			createOrGetElement(lists, clazz, debugMode);
		}
	
		/* Sort and add role singletons. */
	
		lists.sort();
	
		for (OrderingNode<EngineWindowBroadcaster> obj : lists.windowBroadcasters)
		{
			obj.object.addWindowEventReceiver(windowEventReceiver);
			logger.debugf("%s was passed a window event receiver.", obj.object.getClass().getSimpleName());
		}
		
		for (OrderingNode<EngineInputBroadcaster> obj : lists.inputBroadcasters)
		{
			obj.object.addInputReceiver(inputEventReceiver);
			logger.debugf("%s was passed an input event receiver.", obj.object.getClass().getSimpleName());
		}
	
		for (OrderingNode<EngineMessageBroadcaster> obj : lists.messageBroadcasters)
		{
			obj.object.addMessageReceiver(messageReceiver);
			logger.debugf("%s was passed a message receiver.", obj.object.getClass().getSimpleName());
		}
	
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
		
		for (OrderingNode<EngineSettingsListener> obj : lists.settingsListeners)
		{
			settingsListeners.enqueue(obj.object);
			logger.debugf("%s added to settings listeners.", obj.object.getClass().getSimpleName());
		}
	
		for (OrderingNode<EngineReadyListener> obj : lists.readyListeners)
		{
			readyListeners.enqueue(obj.object);
			logger.debugf("%s added to ready listeners.", obj.object.getClass().getSimpleName());
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
		
	}

	private void stopTicker()
	{
		logger.infof("Stopping ticker...");
		updateTicker.stop();
	}

	private void createAllDevices()
	{
		for (ObjectPair<?, EngineDevice> device : devices)
		{
			EngineDevice ed = device.getValue(); 
			logger.infof("Starting device %s.", ed.getDeviceName());
			try {
				if (ed.createDevice())
					logger.infof("Finished starting device %s.", ed.getDeviceName());
				else
					logger.errorf("Failed starting device %s.", ed.getDeviceName());
			} catch (Exception e) {
				handleException(new EngineSetupException("Device \""+ed.getDeviceName()+"\" could not be started.", e));
			}
		}
	}

	private void destroyAllDevices()
	{
		for (ObjectPair<?, EngineDevice> device : devices)
		{
			EngineDevice ed = device.getValue(); 
			logger.infof("Destroying device %s.", ed.getDeviceName());
			if (ed.destroyDevice())
				logger.infof("Finished destroying device %s.", ed.getDeviceName());
			else
				logger.errorf("Failed destroying device %s.", ed.getDeviceName());
		}
	}

	// Loads all archived global variables.
	private void loadGlobalVariables(EngineFileSystem fileSystem)
	{
		Properties settings = null;
		InputStream inStream = null;
	
		if (!Common.isEmpty(config.getGlobalVariablesFile()))
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
			for (EngineSettingsListener listener : settingsListeners)
				listener.onLoadGlobalSettings(settings);
			console.loadGlobalVariables(settings);
		}
	}

	// Loads all archived user variables.
	private void loadUserVariables(EngineFileSystem fileSystem)
	{
		Properties settings = null;
		InputStream inStream = null;
	
		if (!Common.isEmpty(config.getUserVariablesFile()))
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
			for (EngineSettingsListener listener : settingsListeners)
				listener.onLoadUserSettings(settings);
			console.loadUserVariables(settings);
		}
	
	}

	// Saves all archived global variables.
	private void saveGlobalVariables(EngineFileSystem fileSystem)
	{
		String applicationString = 
			(!Common.isEmpty(config.getApplicationName()) ? config.getApplicationName() : "") 
			+ (!Common.isEmpty(config.getApplicationVersion()) ? " v" + config.getApplicationVersion() : "");
		OrderedProperties settings = null;
		OutputStream outStream = null;
		if (!Common.isEmpty(config.getGlobalVariablesFile()))
		{
			logger.infof("Saving global settings...");
			settings = new OrderedProperties();
			for (EngineSettingsListener listener : settingsListeners)
				listener.onSaveGlobalSettings(settings);
			console.saveGlobalVariables(settings);
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

	// Saves all archived user variables.
	private void saveUserVariables(EngineFileSystem fileSystem)
	{
		String applicationString = 
			(!Common.isEmpty(config.getApplicationName()) ? config.getApplicationName() : "") 
			+ (!Common.isEmpty(config.getApplicationVersion()) ? " v" + config.getApplicationVersion() : "");
		OrderedProperties settings = null;
		OutputStream outStream = null;
	
		if (!Common.isEmpty(config.getUserVariablesFile()))
		{
			logger.infof("Saving user settings...");
			settings = new OrderedProperties();
			for (EngineSettingsListener listener : settingsListeners)
				listener.onSaveUserSettings(settings);
			console.saveUserVariables(settings);
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
	 * Creates a new component for a class and using one of its constructors.
	 * @param lists the set of ordering lists to use for ordering sorting.
	 * @param clazz the class to instantiate.
	 * @param constructor the constructor to call for instantiation.
	 * @param debugMode if true, processes CVARs and CCMDs only available in debug mode.
	 * @return the new class instance.
	 */
	private <T> T createElement(OrderingLists lists, Class<T> clazz, Constructor<T> constructor, boolean debugMode)
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
					params[i] = createOrGetElement(lists, types[i], debugMode);
			}
			
			object = Reflect.construct(constructor, params);
			
			singletonsConstructing.remove(clazz);
		}
	
		if (!clazz.isAnnotationPresent(EngineElement.class))
			return object;
		
		console.addEntries(object, debugMode);
	
		lists.add(object);
		
		return object;
	}

	/**
	 * Creates or gets an engine singleton component by class.
	 * @param lists the set of ordering lists to use for ordering sorting.
	 * @param clazz the class to create/retrieve.
	 * @param debugMode if true, processes CVARs and CCMDs only available in debug mode.
	 */
	@SuppressWarnings("unchecked")
	private <T> T createOrGetElement(OrderingLists lists, Class<T> clazz, boolean debugMode)
	{
		if (singletons.containsKey(clazz))
			return (T)singletons.get(clazz);
		
		T instance = createElement(lists, clazz, EngineUtils.getAnnotatedConstructor(clazz), debugMode);
		singletons.put(clazz, instance);
		logger.infof("Created element. %s", clazz.getSimpleName());
		return instance;
	}

	/**
	 * Creates an engine device. 
	 * @param name the name of the device.
	 * @return true if successful, false if not, not found, or device was already active.
	 */
	private boolean createDevice(String name)
	{
		EngineDevice device = devices.get(name);
		if (device != null)
		{
			if (device.isDeviceActive())
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
	 * Destroys an engine device. 
	 * @param name the name of the device.
	 * @return true if successful, false if not, not found, or device was not active.
	 */
	private boolean destroyDevice(String name)
	{
		EngineDevice device = devices.get(name);
		if (device != null)
		{
			if (!device.isDeviceActive())
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

	/**
	 * Creates or gets an engine singleton component by class.
	 * @param clazz the class to create/retrieve.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getElement(Class<T> clazz)
	{
		if (singletons.containsKey(clazz))
			return (T)singletons.get(clazz);
		
		return null;
	}

	/**
	 * Creates or gets an engine resource by class and name.
	 * @param clazz the class to use.
	 * @param name the name of the resource.
	 * @return the corresponding resource or null if not found.
	 */
	public <T extends EngineResource> T getResource(Class<T> clazz, String name)
	{
		return getElement(EngineResources.class).getResource(clazz, name);
	}

	/**
	 * Creates or gets an engine resource set by class.
	 * @param clazz the class to use.
	 * @return the corresponding set or null if not found.
	 */
	public <T extends EngineResource> ResourceSet<T> getResourceSet(Class<T> clazz)
	{
		return getElement(EngineResources.class).getResourceSet(clazz);
	}

	/**
	 * Saves user and global settings to persistent storage.
	 * Calls the following: 
	 * {@link EngineSettingsListener#onSaveUserSettings(Properties)}, 
	 * {@link EngineSettingsListener#onSaveGlobalSettings(Properties)},
	 * {@link EngineConsole#saveUserVariables(Properties)},
	 * {@link EngineConsole#saveGlobalVariables(Properties)}
	 */
	public void saveSettings()
	{
		EngineFileSystem fileSystem = getElement(EngineFileSystem.class);
		saveUserVariables(fileSystem);
		saveGlobalVariables(fileSystem);
	}
	
	/**
	 * Loads user and global settings from persistent storage and sets element properties.
	 * Calls the following: 
	 * {@link EngineSettingsListener#onLoadUserSettings(Properties)}, 
	 * {@link EngineSettingsListener#onLoadGlobalSettings(Properties)},
	 * {@link EngineConsole#loadUserVariables(Properties)},
	 * {@link EngineConsole#loadGlobalVariables(Properties)}
	 */
	public void loadSettings()
	{
		EngineFileSystem fileSystem = getElement(EngineFileSystem.class);
		loadUserVariables(fileSystem);
		loadGlobalVariables(fileSystem);
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
	
	/**
	 * Handles an uncaught, fatal exception and initiates engine shutdown.
	 * <p>The ticker is stopped, all devices have {@link EngineDevice#destroyDevice()} called on them, 
	 * all listeners have {@link EngineShutdownListener#onUnexpectedEngineShutDown(Throwable)} called on them, and tells the JVM to exit.
	 * @param t the throwable that caused this to be called.
	 */
	public void handleException(Throwable t)
	{
		logger.severe(t, "Uncaught exception thrown: " + t.getClass().getSimpleName() +": " + t.getLocalizedMessage());
	
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
	 * all devices have {@link EngineDevice#destroyDevice()} called on them, and tells the JVM to exit.
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
	 * all devices have {@link EngineDevice#destroyDevice()} called on them, and tells the JVM to exit.
	 * @param status the status code to return upon program completion (aka "errorlevel" on some OSes).
	 */
	public void shutDown(int status)
	{
		logger.infof("Shutdown initiated.");
	
		stopTicker();

		saveSettings();
	
		logger.infof("Notifying listeners...");
		for (EngineShutdownListener listener : shutdownListeners)
			listener.onEngineShutdown();
	
		destroyAllDevices();
		
		logger.infof("Shutting down JVM. Bye!");
		System.exit(status);
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
			output(String.format("[%s] %-7s <%s> %s", DATE_FORMAT.format(time), level.name(), source, message));
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
		 * @param line the line to output. 
		 */
		protected abstract void output(String line);
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

	/** Node for ordering lists of components. */
	private static class OrderingLists
	{
		private List<OrderingNode<EngineDevice>> devices;
		private List<OrderingNode<EngineWindowBroadcaster>> windowBroadcasters;
		private List<OrderingNode<EngineInputBroadcaster>> inputBroadcasters;
		private List<OrderingNode<EngineMessageBroadcaster>> messageBroadcasters;
		private List<OrderingNode<EngineWindowListener>> windowListeners;
		private List<OrderingNode<EngineInputListener>> inputListeners;
		private List<OrderingNode<EngineMessageListener>> messageListeners;
		private List<OrderingNode<EngineSettingsListener>> settingsListeners;
		private List<OrderingNode<EngineReadyListener>> readyListeners;
		private List<OrderingNode<EngineShutdownListener>> shutdownListeners;
		private List<OrderingNode<EngineUpdateListener>> updateListeners;
		
		private OrderingLists()
		{
			devices = new List<Engine.OrderingNode<EngineDevice>>();
			windowBroadcasters = new List<Engine.OrderingNode<EngineWindowBroadcaster>>();
			inputBroadcasters = new List<Engine.OrderingNode<EngineInputBroadcaster>>();
			messageBroadcasters = new List<Engine.OrderingNode<EngineMessageBroadcaster>>();
			windowListeners = new List<Engine.OrderingNode<EngineWindowListener>>();
			inputListeners = new List<Engine.OrderingNode<EngineInputListener>>();
			messageListeners = new List<Engine.OrderingNode<EngineMessageListener>>();
			settingsListeners = new List<Engine.OrderingNode<EngineSettingsListener>>();
			readyListeners = new List<Engine.OrderingNode<EngineReadyListener>>();
			shutdownListeners = new List<Engine.OrderingNode<EngineShutdownListener>>();
			updateListeners = new List<Engine.OrderingNode<EngineUpdateListener>>();
		}
		
		private void sort()
		{
			devices.sort();
			windowBroadcasters.sort();
			inputBroadcasters.sort();
			messageBroadcasters.sort();
			windowListeners.sort();
			inputListeners.sort();
			messageListeners.sort();
			settingsListeners.sort();
			readyListeners.sort();
			shutdownListeners.sort();
			updateListeners.sort();
		}
		
		private <T> void add(T object)
		{
			Class<?> clazz = object.getClass();
			
			Ordering anno = clazz.getAnnotation(Ordering.class);
			int ordering = anno == null ? 0 : anno.value();
			
			// check if engine window broadcaster.
			if (EngineWindowBroadcaster.class.isAssignableFrom(clazz))
			{
				EngineWindowBroadcaster obj = (EngineWindowBroadcaster)object;
				windowBroadcasters.add(new OrderingNode<EngineWindowBroadcaster>(ordering, obj));
			}
					
			// check if engine input broadcaster.
			if (EngineInputBroadcaster.class.isAssignableFrom(clazz))
			{
				EngineInputBroadcaster obj = (EngineInputBroadcaster)object;
				inputBroadcasters.add(new OrderingNode<EngineInputBroadcaster>(ordering, obj));
			}

			// check if engine message broadcaster.
			if (EngineMessageBroadcaster.class.isAssignableFrom(clazz))
			{
				EngineMessageBroadcaster obj = (EngineMessageBroadcaster)object;
				messageBroadcasters.add(new OrderingNode<EngineMessageBroadcaster>(ordering, obj));
			}

			// check if device.
			if (EngineDevice.class.isAssignableFrom(clazz))
			{
				EngineDevice obj = (EngineDevice)object;
				devices.add(new OrderingNode<EngineDevice>(ordering, obj));
			}
		
			// check if engine listener.
			if (EngineWindowListener.class.isAssignableFrom(clazz))
			{
				EngineWindowListener obj = (EngineWindowListener)object;
				windowListeners.add(new OrderingNode<EngineWindowListener>(ordering, obj));
			}
		
			// check if message listener.
			if (EngineMessageListener.class.isAssignableFrom(clazz))
			{
				EngineMessageListener obj = (EngineMessageListener)object;
				messageListeners.add(new OrderingNode<EngineMessageListener>(ordering, obj));
			}
		
			// check if input listener.
			if (EngineInputListener.class.isAssignableFrom(clazz))
			{
				EngineInputListener obj = (EngineInputListener)object;
				inputListeners.add(new OrderingNode<EngineInputListener>(ordering, obj));
			}
			
			// check if input listener.
			if (EngineShutdownListener.class.isAssignableFrom(clazz))
			{
				EngineShutdownListener obj = (EngineShutdownListener)object;
				shutdownListeners.add(new OrderingNode<EngineShutdownListener>(ordering, obj));
			}
			
			// check if engine ready listener.
			if (EngineReadyListener.class.isAssignableFrom(clazz))
			{
				EngineReadyListener obj = (EngineReadyListener)object;
				readyListeners.add(new OrderingNode<EngineReadyListener>(ordering, obj));
			}
		
			// check if settings listener.
			if (EngineSettingsListener.class.isAssignableFrom(clazz))
			{
				EngineSettingsListener obj = (EngineSettingsListener)object;
				settingsListeners.add(new OrderingNode<EngineSettingsListener>(ordering, obj));
			}
		
			// check if update listener.
			if (EngineUpdateListener.class.isAssignableFrom(clazz))
			{
				EngineUpdateListener obj = (EngineUpdateListener)object;
				updateListeners.add(new OrderingNode<EngineUpdateListener>(ordering, obj));
			}
		
		}
		
	}
	
}
