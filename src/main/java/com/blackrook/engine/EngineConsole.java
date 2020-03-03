/*******************************************************************************
 * Copyright (c) 2016-2020 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;

import com.blackrook.engine.annotation.element.CCMD;
import com.blackrook.engine.annotation.element.CVAR;
import com.blackrook.engine.exception.ConsoleCommandInvocationException;
import com.blackrook.engine.exception.ConsoleSetupException;
import com.blackrook.engine.exception.ConsoleVariableException;
import com.blackrook.engine.struct.ArgumentTokenizer;
import com.blackrook.engine.struct.OSUtils;
import com.blackrook.engine.struct.TypeProfileFactory.Profile;
import com.blackrook.engine.struct.TypeProfileFactory.Profile.FieldInfo;
import com.blackrook.engine.struct.TypeProfileFactory.Profile.MethodInfo;
import com.blackrook.engine.struct.Utils;

/**
 * The manager that can call and get/set elements available to the console.
 * @author Matthew Tropiano
 */
public class EngineConsole
{
	/** Engine reference. */
	private Engine engine;
	/** A Trie that holds all auto-completable commands. */
	private TreeSet<String> variableTrie;
	/** A Trie that holds all auto-completable commands. */
	private TreeSet<String> commandTrie;

	/** Mapping of commands to invocation targets. */
	private TreeMap<String, CCMDMapping> commandMap;
	/** Longest command length. */
	private int commandLongestLength;
	/** Mapping of variables to variable fields/methods. */
	private TreeMap<String, CVARMapping> variableMap;
	/** Longest variable length. */
	private int variableLongestLength;

	/**
	 * Default constructor.
	 */
	EngineConsole(Engine engine, EngineConfig config)
	{
		this.engine = engine;
		commandTrie = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		variableTrie = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		commandMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		commandLongestLength = 0;
		variableMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		variableLongestLength = 0;
	}
	
	/**
	 * Adds the entries for commands and variables to the console manager.
	 * @param instance the object instance to inspect for commands and variables.
	 * @param debug if true, add debug commands and variables. 
	 */
	public void addEntries(Object instance, boolean debug)
	{
		Class<?> type = instance.getClass();
		Profile<?> profile = Utils.getProfile(type);
		
		// add commands.
		for (Method method : type.getMethods())
		{
			CCMD anno = null;
			if ((anno = method.getAnnotation(CCMD.class)) == null)
				continue;
			
			if (anno.debug() && !debug)
				continue;
			
			String cmdname = (Utils.isEmpty(anno.value()) ? method.getName().toLowerCase() : anno.value()).toLowerCase();

			if (commandMap.containsKey(cmdname))
			{
				CCMDMapping declaring = commandMap.get(cmdname);
				throw new ConsoleSetupException("Command \""+cmdname+"\" already declared by "+declaring.method.toGenericString());
			}
			
			commandMap.put(cmdname, new CCMDMapping(instance, method, anno.description(), anno.usage()));
			commandTrie.add(cmdname);
			commandLongestLength = Math.max(commandLongestLength, cmdname.length());
		}

		// add variables.
		for (Map.Entry<String, FieldInfo> pair : profile.getPublicFieldsByName().entrySet())
		{
			String fieldName = pair.getKey();
			FieldInfo signature = pair.getValue();
			Field field = signature.getField();
			
			CVAR anno = field.getAnnotation(CVAR.class);
			if (anno == null)
				continue;
			
			String varname = (Utils.isEmpty(anno.value()) ? fieldName : anno.value()).toLowerCase();

			if (variableMap.containsKey(varname))
			{
				CVARMapping declaring = variableMap.get(varname);
				if (declaring.field != null)
					throw new ConsoleSetupException("Variable \""+varname+"\" already declared by field "+declaring.field.toGenericString());
				else if (declaring.getter != null)
					throw new ConsoleSetupException("Variable \""+varname+"\" already declared by getter "+declaring.getter.toGenericString());
				else if (declaring.setter != null)
					throw new ConsoleSetupException("Variable \""+varname+"\" already declared in setter "+declaring.getter.toGenericString());
			}
			
			variableMap.put(varname, new CVARMapping(instance, anno.description(), anno.archived(), anno.global(), field));
			variableTrie.add(varname);
			variableLongestLength = Math.max(variableLongestLength, varname.length());
		}
		
		for (Map.Entry<String, MethodInfo> pair : profile.getGetterMethodsByName().entrySet())
		{
			String getterName = pair.getKey();
			MethodInfo signature = pair.getValue();
			Method method = signature.getMethod();

			CVAR anno = method.getAnnotation(CVAR.class);
			if (anno == null)
				continue;
			
			String varname = (Utils.isEmpty(anno.value()) ? getterName : anno.value()).toLowerCase();

			if (variableMap.containsKey(varname))
			{
				CVARMapping declaring = variableMap.get(varname);
				if (declaring.field != null)
					throw new ConsoleSetupException("Variable \""+varname+"\" already declared by field "+declaring.field.toGenericString());
				else if (declaring.getter != null)
					throw new ConsoleSetupException("Variable \""+varname+"\" already declared by getter "+declaring.getter.toGenericString());
			}
			
			variableMap.put(varname, new CVARMapping(instance, anno.description(), anno.archived(), anno.global(), method));
			variableTrie.add(varname);
			variableLongestLength = Math.max(variableLongestLength, varname.length());
		}
		
		for (Map.Entry<String, MethodInfo> pair : profile.getSetterMethodsByName().entrySet())
		{
			String setterName = pair.getKey();
			MethodInfo signature = pair.getValue();
			Method method = signature.getMethod();

			CVAR anno = method.getAnnotation(CVAR.class);
			if (anno == null)
				continue;

			String varname = (Utils.isEmpty(anno.value()) ? setterName : anno.value()).toLowerCase();
			
			if (variableMap.containsKey(varname))
			{
				CVARMapping declaring = variableMap.get(varname);
				if (declaring.field != null)
					throw new ConsoleSetupException("Variable \""+varname+"\" already declared by field "+declaring.field.toGenericString());
				else if (declaring.setter != null)
					throw new ConsoleSetupException("Variable setter for \""+varname+"\" already declared by setter "+declaring.setter.toGenericString());
			}
			
			CVARMapping mapping = variableMap.get(varname);
			if (mapping == null)
				throw new ConsoleSetupException("Variable \""+varname+"\" is missing a pairing!"); 
			mapping.setter = method;
		}
		
		// scan for incomplete CVAR support. 
		for (Map.Entry<String, CVARMapping> pair : variableMap.entrySet())
		{
			CVARMapping mapping = pair.getValue();
			if (mapping.field == null)
			{
				if (mapping.getter == null)
					throw new ConsoleSetupException("Variable \""+pair.getKey()+"\" has no getter, hence, no read method! Cannot have a write-only variable!"); 
			}
		}
	}
	 
	/**
	 * Checks if a variable exists by name.
	 * @param name the name of the variable.
	 * @return true if it exists, false if not.
	 */
	public boolean containsVariable(String name)
	{
		return variableMap.containsKey(name);
	}

	/**
	 * Gets all of the variable names known to the console.
	 * @return all variable names in an array.
	 */
	public String[] getVariableNames()
	{
		String[] out = new String[variableMap.size()];
		Iterator<Map.Entry<String, CVARMapping>> it = variableMap.entrySet().iterator();
		int i = 0;
		while (it.hasNext())
			out[i++] = it.next().getKey();
		return out;
	}

	/**
	 * Returns all variable names that start with a string.
	 * @param prefix the start of the variable name.
	 * @return the matching variable names in an array.
	 */
	public String[] getVariableNamesForPrefix(String prefix)
	{
		prefix = prefix.toLowerCase();
		List<String> outList = new LinkedList<String>();
		for (String s : variableTrie.tailSet(prefix, false))
			if (s.toLowerCase().startsWith(prefix))
				outList.add(s);
		String[] out = new String[outList.size()];
		Iterator<String> it = outList.iterator();
		int i = 0;
		while (it.hasNext())
			out[i++] = it.next();
		return out;
	}
	
	/**
	 * Sets all variables and their values using the entries on a {@link Properties} object. 
	 * @param settings the properties object to set variable values on.
	 * @see #setVariable(String, Object)
	 * @see CVAR
	 */
	public void loadUserVariables(Properties settings)
	{
		Object settingValue = null;
		for (String var : getVariableNames(true, false))
		{
			if ((settingValue = settings.getProperty(var)) != null)
				setVariable(var, settingValue);
		}
	}
	
	/**
	 * Sets all variables and their values using the entries on a {@link Properties} object. 
	 * @param settings the properties object to set variable values on.
	 * @see #setVariable(String, Object)
	 * @see CVAR
	 */
	public void loadGlobalVariables(Properties settings)
	{
		Object settingValue = null;
		for (String var : getVariableNames(true, true))
		{
			if ((settingValue = settings.getProperty(var)) != null)
				setVariable(var, settingValue);
		}
	}
	
	/**
	 * Puts all user variables and their values into a {@link Properties} object. 
	 * This is called when variables need to be saved to storage. 
	 * @param settings the properties object to set variable values on.
	 * @see #getVariable(String)
	 * @see CVAR
	 */
	public void saveUserVariables(Properties settings)
	{
		for (String var : getVariableNames(true, false))
		{
			String s = getVariable(var, String.class);
			if (s != null)
				settings.setProperty(var, s);
		}
	}
	
	/**
	 * Puts all global variables and their values into a {@link Properties} object.
	 * This is called when variables need to be saved to storage. 
	 * @param settings the properties object to set variable values on.
	 * @see #getVariable(String)
	 * @see CVAR
	 */
	public void saveGlobalVariables(Properties settings)
	{
		for (String var : getVariableNames(true, true))
		{
			String s = getVariable(var, String.class);
			if (s != null)
				settings.setProperty(var, s);
		}
	}
	
	/**
	 * Returns all variable names in an array according to some criteria.
	 * @param archived if true, get variables that will be saved/persisted to storage.
	 * @param global if true, get global variables. false, user variables.
	 */
	private String[] getVariableNames(boolean archived, boolean global)
	{
		List<String> outList = new LinkedList<>();
		Iterator<Map.Entry<String, CVARMapping>> it = variableMap.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry<String, CVARMapping> pair = it.next();
			String name = pair.getKey();
			CVARMapping mapping = pair.getValue();
			if (mapping.archived == archived && mapping.global == global)
				outList.add(name);
		}
		String[] out = new String[outList.size()];
		outList.toArray(out);
		return out;
	}

	/**
	 * Gets a variable definition.
	 * @param name the name of the variable.
	 * @return the variable mapping object for a variable.
	 */
	public CVARMapping getVariableDefinition(String name)
	{
		return variableMap.get(name);
	}

	/**
	 * Gets the value of a variable by name.
	 * @param name the name of the variable.
	 * @return the value of the variable.
	 */
	public Object getVariable(String name)
	{
		synchronized (variableMap)
		{
			return variableMap.containsKey(name) ? variableMap.get(name).get() : null;
		}
	}
	
	/**
	 * Gets the value of a variable by name, converted to a value type.
	 * @param <T> the return/conversion type.
	 * @param name the name of the variable.
	 * @param type the target type.
	 * @return the value of the variable, converted.
	 * @throws ClassCastException if the read value cannot be converted.
	 */
	public <T> T getVariable(String name, Class<T> type)
	{
		return variableMap.containsKey(name) ? Utils.createForType(variableMap.get(name).get(), type) : null; 
	}

	/**
	 * Sets the value of a variable.
	 * @param name the name of the variable.
	 * @param value the value to set.
	 * @throws ConsoleVariableException if the variable doesn't exist or the variable is read-only.
	 * @throws ClassCastException if the incoming value cannot be converted.
	 */
	public void setVariable(String name, Object value)
	{
		synchronized (variableMap)
		{
			if (!variableMap.containsKey(name))
				throw new ConsoleVariableException("Variable \""+name+"\" doesn't exist.");
			else
				variableMap.get(name).set(value); 
		}
	}
	
	/**
	 * Checks if a command exists.
	 * @param name the name of the command.
	 * @return true if it exists, false if not.
	 */
	public boolean containsCommand(String name)
	{
		return commandMap.containsKey(name);
	}
	
	/**
	 * Gets all of the command names known to the console.
	 * @return all command names in an array.
	 */
	public String[] getCommandNames()
	{
		String[] out = new String[commandMap.size()];
		Iterator<Map.Entry<String, CCMDMapping>> it = commandMap.entrySet().iterator();
		int i = 0;
		while (it.hasNext())
			out[i++] = it.next().getKey();
		return out;
	}
	
	/**
	 * Returns all command names that start with a string.
	 * @param prefix the start of the command name.
	 * @return the matching command names in an array.
	 */
	public String[] getCommandNamesForPrefix(String prefix)
	{
		prefix = prefix.toLowerCase();
		List<String> outList = new LinkedList<String>();
		for (String s : commandTrie.tailSet(prefix, false))
			if (s.toLowerCase().startsWith(prefix))
				outList.add(s);
		String[] out = new String[outList.size()];
		Iterator<String> it = outList.iterator();
		int i = 0;
		while (it.hasNext())
			out[i++] = it.next();
		return out;
	}
	
	/**
	 * Gets a command definition.
	 * @param name the name of the command.
	 * @return the command mapping object for a command.
	 */
	public CCMDMapping getCommandDefinition(String name)
	{
		return commandMap.get(name);
	}
	
	/**
	 * Calls a command.
	 * @param name the name of the command.
	 * @param args the command arguments.
	 * @return the command return value.
	 */
	public Object callCommand(String name, Object ... args)
	{
		if (!commandMap.containsKey(name))
			throw new ConsoleCommandInvocationException("Command \""+name+"\" doesn't exist.");
		else
			return commandMap.get(name).call(args);
	}
	
	/** 
	 * Returns the length of the command with the longest name.
	 * @return the length in characters. 
	 */
	public int getCommandLongestLength()
	{
		return commandLongestLength;
	}

	/** 
	 * Returns the length of the variable with the longest name. 
	 * @return the length in characters. 
	 */
	public int getVariableLongestLength()
	{
		return variableLongestLength;
	}

	/**
	 * Parses a command. Can execute multiple commands at once.
	 * @param commandString the typed command to parse/process.
	 */
	public void parseCommand(String commandString)
	{
		commandString = commandString.trim();
		char[] cmdchars = commandString.toCharArray();
		StringBuilder sb = new StringBuilder();
		
		final int STATE_INIT = 0;
		final int STATE_INQUOTE = 1;
		final int STATE_INQUOTE_ESCAPE = 2;
		int state = STATE_INIT;
		
		for (int i = 0; i < cmdchars.length; i++)
		{
			char c = cmdchars[i];
			
			switch (state)
			{
				case STATE_INIT:
				{
					if (c == '"')
					{
						sb.append(c);
						state = STATE_INQUOTE;
					}
					else if (c == ';')
					{
						processCommand(sb.toString());
						sb.delete(0, sb.length());
					}
					else
						sb.append(c);
				}
				break;
					
				case STATE_INQUOTE:
				{
					if (c == '"')
					{
						sb.append(c);
						state = STATE_INIT;
					}
					else if (c == '\\')
					{
						sb.append(c);
						state = STATE_INQUOTE_ESCAPE;
					}
					else
						sb.append(c);					
				}
				break;
	
				case STATE_INQUOTE_ESCAPE:
				{
					sb.append(c);
					state = STATE_INIT;
				}
				break;
				
			}
			
		}
	
		if (sb.length() > 0)
			processCommand(sb.toString());
		
	}

	/**
	 * Processes a single command.
	 * @param commandString the command string to process.
	 */
	public void processCommand(String commandString)
	{
		if (Utils.isEmpty(commandString))
			return;
			
		ArgumentTokenizer tokenizer = new ArgumentTokenizer(commandString);
		String cmd = tokenizer.nextToken();
		List<String> argList = new LinkedList<String>();
		String token = null;
		while ((token = tokenizer.nextToken()) != null)
			argList.add(token);
		
		String[] args = new String[argList.size()];
		argList.toArray(args);
		
		Object out = null;
		try {
			
			if (containsCommand(cmd))
				out = callCommand(cmd, (Object[])args);
			else if (containsVariable(cmd))
			{
				if (args.length == 0)
					println(cmd + " is " + getVariableRepresentation(getVariable(cmd)));
				else
				{
					setVariable(cmd, args[0]);
					println(cmd + " set to " + getVariableRepresentation(getVariable(cmd)));
				}
			}
			else
				println("ERROR: " + cmd + " is not a command, alias, or variable.");
		
		} catch (ConsoleCommandInvocationException ex) {
	
			println("ERROR: " + ex.getMessage());
			CCMDMapping mapping = getCommandDefinition(cmd);
			String[] usage = mapping.getUsage();
			
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < usage.length; i++)
			{
				sb.append('[').append(usage[i]).append(']');
				if (i < usage.length - 1)
					sb.append(' ');
			}
			println("Usage: " + cmd + " " + sb.toString());
	
		} catch (Exception ex) {
			println("EXCEPTION: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
			println(Utils.getExceptionString(ex));
		}
		
		if (out != null)
			println(String.valueOf(out));
	}

	/**
	 * Prints a message to the engine console.
	 * @param object the message to print (see {@link String#valueOf(Object)}).
	 */
	public void print(Object object)
	{
		engine.consolePrint(object);
	}

	/**
	 * Prints a formatted message to the console.
	 * @param formatting the format text (see {@link String#format(String, Object...)}).
	 * @param args the arguments to use.
	 */
	public void printf(String formatting, Object ... args)
	{
		print(String.format(formatting, args));
	}

	/**
	 * Prints a message to the console with a newline appended to it.
	 * @param object the message to print (see {@link String#valueOf(Object)}).
	 */
	public void println(Object object)
	{
		print(String.valueOf(object) + '\n');
	}

	/**
	 * Prints a newline.
	 */
	public void println()
	{
		print('\n');
	}

	/**
	 * Prints a formatted message to the console with a newline appended to it.
	 * @param formatting the format text (see {@link String#format(String, Object...)}).
	 * @param args the message to print (see {@link String#valueOf(Object)}).
	 */
	public void printfln(String formatting, Object ... args)
	{
		print(String.format(formatting, args) + '\n');
	}

	// returns variable representation.
	private String getVariableRepresentation(Object obj)
	{
		if (obj == null)
			return "null";
		else if (obj instanceof CharSequence)
			return '"' + String.valueOf(obj) + '"';
		else if (obj instanceof Character)
			return '\'' + String.valueOf(obj) + '\'';
		else if (obj instanceof Boolean || obj instanceof Number)
			return String.valueOf(obj);
		else
			return String.valueOf(obj);
	}

	@CCMD(description = "Tells the engine to shut down.")
	public void quit(int status)
	{
		engine.shutDown(status);
	}

	@CCMD(description = "Echos a line to the console out.")
	public void echo(String text)
	{
		println(text);
	}

	@CCMD(description = "Dumps all console variables to console.")
	public void cvarList(String text)
	{
		String[] variables = getVariableNames();
		
		Arrays.sort(variables);
		int maxlen = getVariableLongestLength();
		int i = 0;
		for (String var : variables)
		{
			CVARMapping mapping = getVariableDefinition(var);
			printfln(
				"%s%s%s %-"+maxlen+"s %s",  
				mapping.isArchived() ? "A" : "-", 
				mapping.isGlobal() ? "G" : "-", 
				mapping.isReadOnly() ? "R" : "-", 
				var, 
				mapping.getDescription()
			);
			i++;
		}
		
		printfln("count %d", i);
	}

	@CCMD(description = "Lists all console commands.")
	public void cmdList(String prefix)
	{
		String[] commands = null;
		if (Utils.isEmpty(prefix))
			commands = getCommandNames();
		else
			commands = getCommandNamesForPrefix(prefix);
		
		Arrays.sort(commands);
		int maxlen = getCommandLongestLength();
	
		int i = 0;
		for (String cmd : commands)
		{
			printfln("%-"+maxlen+"s %s", cmd, getCommandDefinition(cmd).getDescription());
			i++;
		}
		
		printfln("count %d", i);
	}

	@CVAR(value = "java_version", description = "JVM version.")
	public String getJavaVersion()
	{
		return System.getProperty("java.version");
	}

	@CVAR(value = "java_vendor", description = "JVM vendor.")
	public String getJavaVendor()
	{
		return System.getProperty("java.vendor");
	}

	@CVAR(value = "java_vm_name", description = "JVM name.")
	public String getJavaVMName()
	{
		return System.getProperty("java.vm.name");
	}

	@CVAR(value = "java_classpath", description = "JVM current classpath.")
	public String getJavaClasspath()
	{
		return System.getProperty("java.class.path");
	}

	@CVAR(value = "java_libpath", description = "JVM native library path.")
	public String getJavaLibraryPath()
	{
		return System.getProperty("java.library.path");
	}

	@CVAR(value = "os_name", description = "OS name.")
	public String getOSName()
	{
		return System.getProperty("os.name");
	}

	@CVAR(value = "os_version", description = "OS version name.")
	public String getOSVersion()
	{
		return System.getProperty("os.version");
	}

	@CVAR(value = "os_arch", description = "OS architecture type.")
	public String getOSArchitecture()
	{
		return System.getProperty("os.arch");
	}

	@CVAR(value = "workdir", description = "Working directory path.")
	public String getWorkingDirectory()
	{
		return OSUtils.getWorkingDirectoryPath();
	}

	@CVAR(value = "appdata", description = "Application data directory path.")
	public String getApplicationDataDirectory()
	{
		return OSUtils.getApplicationSettingsPath();
	}

	@CVAR(value = "homedir", description = "Home directory path.")
	public String getHomeDirectory()
	{
		return OSUtils.getHomeDirectoryPath();
	}

	/**
	 * Mapping for console commands to methods. 
	 */
	public static class CCMDMapping
	{
		/** Instance target to use for invocation. */
		Object instance;
		/** Method to call. */
		Method method;
		/** Command description. */
		String description;
		/** Usage descriptor. */
		String[] usage;

		/** Parameter types. */
		Class<?> types[];
		
		CCMDMapping(Object instance, Method method, String descripton, String[] usage)
		{
			this.instance = instance;
			this.method = method;
			this.description = descripton;
			this.usage = usage;
			types = method.getParameterTypes();
		}
		
		/**
		 * Gets the description of this command.
		 * @return the description.
		 */
		public String getDescription()
		{
			return description;
		}

		/**
		 * Gets the usage blurb of this command.
		 * @return the usage entries.
		 */
		public String[] getUsage()
		{
			return usage;
		}

		Object call(Object ... args)
		{
			Object[] params = new Object[types.length];
			int i = 0;
			for (; i < Math.min(args.length, params.length); i++)
				params[i] = Utils.createForType(args[i], types[i]);
			for (; i < params.length; i++)
				params[i] = Utils.createForType(null, types[i]);
			return Utils.invokeBlind(method, instance, params);
		}
		
	}
	
	/**
	 * Mapping for console variables to methods. 
	 */
	public static class CVARMapping
	{
		/** Instance target to use for invocation. */
		Object instance;
		/** Command description. */
		String description;
		/** Archived? */
		boolean archived;
		/** Global? */
		boolean global;
		
		/** Field to change. */
		Field field;
		/** Getter method to call. */
		Method getter;
		/** Setter method to call. */
		Method setter;
		
		/** Type to set. */
		Class<?> type;
		
		CVARMapping(Object instance, String descripton, boolean archived, boolean global, Field field)
		{
			this.instance = instance;
			this.description = descripton;
			this.archived = archived;
			this.global = global;
			this.field = field;
			this.type = field.getType();
		}
		
		CVARMapping(Object instance, String descripton, boolean archived, boolean global, Method getter)
		{
			this.instance = instance;
			this.description = descripton;
			this.archived = archived;
			this.global = global;
			this.getter = getter;
			this.setter = null;
			this.type = getter.getReturnType();
		}

		CVARMapping(Object instance, String descripton, boolean archived, boolean global, Method getter, Method setter)
		{
			this.instance = instance;
			this.description = descripton;
			this.archived = archived;
			this.global = global;
			this.getter = getter;
			this.setter = setter;
			this.type = getter.getReturnType();
		}

		/**
		 * Gets the description of this command.
		 * @return the description.
		 */
		public String getDescription()
		{
			return description;
		}

		/**
		 * Gets if this variable is to be archived.
		 * @return true if so, false if not.
		 */
		public boolean isArchived()
		{
			return archived;
		}

		/**
		 * Gets if this variable is to stored/read from global.
		 * @return true if so, false if not.
		 */
		public boolean isGlobal()
		{
			return global;
		}
		
		/**
		 * Gets if this variable is to be archived.
		 * @return true if so, false if not.
		 */
		public boolean isReadOnly()
		{
			return field == null && setter == null && getter != null;
		}

		Object get()
		{
			if (field != null)
				return Utils.getFieldValue(instance, field);
			else
				return Utils.invokeBlind(getter, instance);
		}

		void set(Object value)
		{
			if (field != null)
				Utils.setFieldValue(instance, field, Utils.createForType(value, type));
			else if (setter != null)
				Utils.invokeBlind(setter, instance, Utils.createForType(value, type));
			else
				throw new ConsoleVariableException("This variable is read-only.");
		}
		
	}
	
}
