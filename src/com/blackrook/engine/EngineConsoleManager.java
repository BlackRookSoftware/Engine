package com.blackrook.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;

import com.blackrook.commons.Common;
import com.blackrook.commons.ObjectPair;
import com.blackrook.commons.Reflect;
import com.blackrook.commons.TypeProfile;
import com.blackrook.commons.TypeProfile.MethodSignature;
import com.blackrook.commons.hash.CaseInsensitiveHashMap;
import com.blackrook.commons.list.List;
import com.blackrook.commons.trie.CaseInsensitiveTrie;
import com.blackrook.engine.annotation.component.CCMD;
import com.blackrook.engine.annotation.component.CVAR;
import com.blackrook.engine.exception.ConsoleCommandInvocationException;
import com.blackrook.engine.exception.ConsoleSetupException;
import com.blackrook.engine.exception.ConsoleVariableException;

/**
 * The manager that can call and get/set elements available to the console.
 * @author Matthew Tropiano
 */
public class EngineConsoleManager
{
	/** A Trie that holds all auto-completable commands. */
	private CaseInsensitiveTrie commandTrie;

	/** Mapping of commands to invocation targets. */
	private CaseInsensitiveHashMap<CCMDMapping> commandMap;
	/** Longest command length. */
	private int commandLongestLength;
	/** Mapping of variables to variable fields/methods. */
	private CaseInsensitiveHashMap<CVARMapping> variableMap;
	/** Longest variable length. */
	private int variableLongestLength;

	/**
	 * Default constructor.
	 */
	EngineConsoleManager()
	{
		commandTrie = new CaseInsensitiveTrie();
		commandMap = new CaseInsensitiveHashMap<CCMDMapping>();
		commandLongestLength = 0;
		variableMap = new CaseInsensitiveHashMap<CVARMapping>();
		variableLongestLength = 0;
	}
	
	/**
	 * Adds the entries for commands and variables to the console manager.
	 */
	public void addEntries(Object instance, boolean debug)
	{
		Class<?> type = instance.getClass();
		TypeProfile<?> profile = TypeProfile.getTypeProfile(type);
		
		// add commands.
		for (Method method : type.getMethods())
		{
			CCMD anno = null;
			if ((anno = method.getAnnotation(CCMD.class)) == null)
				continue;
			
			if (anno.debug() && !debug)
				continue;
			
			String cmdname = (Common.isEmpty(anno.value()) ? method.getName().toLowerCase() : anno.value()).toLowerCase();

			if (commandMap.containsKey(cmdname))
			{
				CCMDMapping declaring = commandMap.get(cmdname);
				throw new ConsoleSetupException("Command \""+cmdname+"\" already declared by "+declaring.method.toGenericString());
			}
			
			commandMap.put(cmdname, new CCMDMapping(instance, method, anno.description(), anno.usage()));
			commandTrie.put(cmdname);
			commandLongestLength = Math.max(commandLongestLength, cmdname.length());
		}

		// add variables.
		for (Field field : profile.getAnnotatedPublicFields(CVAR.class))
		{
			CVAR anno = field.getAnnotation(CVAR.class);
			String varname = Common.isEmpty(anno.value()) ? field.getName() : anno.value();

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
			variableLongestLength = Math.max(variableLongestLength, varname.length());
		}
		
		for (ObjectPair<String, MethodSignature> pair : profile.getGetterMethods())
		{
			String getterName = pair.getKey();
			MethodSignature signature = pair.getValue();
			Method method = signature.getMethod();

			CVAR anno = method.getAnnotation(CVAR.class);
			if (anno == null)
				continue;
			
			String varname = (Common.isEmpty(anno.value()) ? getterName : anno.value()).toLowerCase();

			if (variableMap.containsKey(varname))
			{
				CVARMapping declaring = variableMap.get(varname);
				if (declaring.field != null)
					throw new ConsoleSetupException("Variable \""+varname+"\" already declared by field "+declaring.field.toGenericString());
				else if (declaring.getter != null)
					throw new ConsoleSetupException("Variable \""+varname+"\" already declared by getter "+declaring.getter.toGenericString());
			}
			
			variableMap.put(varname, new CVARMapping(instance, anno.description(), anno.archived(), anno.global(), method));
			variableLongestLength = Math.max(variableLongestLength, varname.length());
		}
		
		for (ObjectPair<String, MethodSignature> pair : profile.getSetterMethods())
		{
			String setterName = pair.getKey();
			MethodSignature signature = pair.getValue();
			Method method = signature.getMethod();

			CVAR anno = method.getAnnotation(CVAR.class);
			if (anno == null)
				continue;

			String varname = (Common.isEmpty(anno.value()) ? setterName : anno.value()).toLowerCase();
			
			if (variableMap.containsKey(varname))
			{
				CVARMapping declaring = variableMap.get(varname);
				if (declaring.field != null)
					throw new ConsoleSetupException("Variable \""+varname+"\" already declared by field "+declaring.field.toGenericString());
				else if (declaring.setter != null)
					throw new ConsoleSetupException("Variable setter for \""+varname+"\" already declared by setter "+declaring.setter.toGenericString());
			}
			
			CVARMapping mapping = variableMap.get(varname);
			mapping.setter = method;
		}
		
		// scan for incomplete CVAR support. 
		for (ObjectPair<String, CVARMapping> pair : variableMap)
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
	 * Returns all variable names in an array.
	 */
	public String[] getVariableNames()
	{
		String[] out = new String[variableMap.size()];
		Iterator<String> it = variableMap.keyIterator();
		int i = 0;
		while (it.hasNext())
			out[i++] = it.next();
		return out;
	}

	/**
	 * Returns all variable names in an array according to some.
	 */
	public String[] getVariableNames(boolean archived, boolean global)
	{
		List<String> outList = new List<>();
		Iterator<ObjectPair<String, CVARMapping>> it = variableMap.iterator();
		while (it.hasNext())
		{
			ObjectPair<String, CVARMapping> pair = it.next();
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
	 */
	public CVARMapping getVariableDefinition(String name)
	{
		return variableMap.get(name);
	}

	/**
	 * Gets the value of a variable by name.
	 * @param name the name of the variable.
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
	 * @param name the name of the variable.
	 * @param type the target type.
	 */
	public <T> T getVariable(String name, Class<T> type)
	{
		return variableMap.containsKey(name) ? Reflect.createForType(variableMap.get(name).get(), type) : null; 
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
	 * Returns all command names in an array.
	 */
	public String[] getCommandNames()
	{
		String[] out = new String[commandMap.size()];
		Iterator<String> it = commandMap.keyIterator();
		int i = 0;
		while (it.hasNext())
			out[i++] = it.next();
		return out;
	}
	
	/**
	 * Returns all command names that start with a string.
	 */
	public String[] getCommandNamesForPrefix(String prefix)
	{
		List<String> outList = new List<String>();
		int amt = commandTrie.getAfter(prefix, outList);
		String[] out = new String[amt];
		Iterator<String> it = outList.iterator();
		int i = 0;
		while (it.hasNext())
			out[i++] = it.next();
		return out;
	}
	
	/**
	 * Gets a variable definition.
	 * @param name the name of the variable.
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
	
	/** Returns the length of the command with the longest name. */
	public int getCommandLongestLength()
	{
		return commandLongestLength;
	}

	/** Returns the length of the variable with the longest name. */
	public int getVariableLongestLength()
	{
		return variableLongestLength;
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
		 */
		public String getDescription()
		{
			return description;
		}

		/**
		 * Gets the usage blurb of this command.
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
				params[i] = Reflect.createForType(args[i], types[i]);
			for (; i < params.length; i++)
				params[i] = Reflect.createForType(null, types[i]);
			return Reflect.invokeBlind(method, instance, params);
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
			type = field.getType();
		}
		
		CVARMapping(Object instance, String descripton, boolean archived, boolean global, Method getter)
		{
			this.instance = instance;
			this.description = descripton;
			this.archived = archived;
			this.global = global;
			this.getter = getter;
			this.setter = null;
			type = getter.getReturnType();
		}

		CVARMapping(Object instance, String descripton, boolean archived, boolean global, Method getter, Method setter)
		{
			this.instance = instance;
			this.description = descripton;
			this.archived = archived;
			this.global = global;
			this.getter = getter;
			this.setter = setter;
			type = getter.getReturnType();
		}

		/**
		 * Gets the description of this command.
		 */
		public String getDescription()
		{
			return description;
		}

		/**
		 * Gets if this variable is to be archived.
		 */
		public boolean isArchived()
		{
			return archived;
		}

		/**
		 * Gets if this variable is to stored/read from global.
		 */
		public boolean isGlobal()
		{
			return global;
		}
		
		/**
		 * Gets if this variable is to be archived.
		 */
		public boolean isReadOnly()
		{
			return field == null && setter == null && getter != null;
		}

		Object get()
		{
			if (field != null)
				return Reflect.getFieldValue(field, instance);
			else
				return Reflect.invokeBlind(getter, instance);
		}

		void set(Object value)
		{
			if (field != null)
				Reflect.setField(instance, field, Reflect.createForType(value, type));
			else if (setter != null)
				Reflect.invokeBlind(setter, instance, Reflect.createForType(value, type));
			else
				throw new ConsoleVariableException("This variable is read-only.");
		}
		
	}
	
}
