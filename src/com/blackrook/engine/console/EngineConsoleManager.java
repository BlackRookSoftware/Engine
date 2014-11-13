package com.blackrook.engine.console;

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
import com.blackrook.engine.annotation.EngineCCMD;
import com.blackrook.engine.annotation.EngineCVAR;
import com.blackrook.engine.annotation.EngineComponent;
import com.blackrook.engine.exception.ConsoleCommandInvocationException;
import com.blackrook.engine.exception.ConsoleSetupException;
import com.blackrook.engine.exception.ConsoleVariableException;

/**
 * The manager that can call and get/set elements available to the console.
 * @author Matthew Tropiano
 */
@EngineComponent
public class EngineConsoleManager
{
	/** A Trie that holds all auto-completable commands. */
	private CaseInsensitiveTrie commandTrie;

	/** Mapping of commands to invocation targets. */
	private CaseInsensitiveHashMap<CCMDMapping> commandMap;
	/** Mapping of variables to variable fields/methods. */
	private CaseInsensitiveHashMap<CVARMapping> variableMap;
	/** Mapping of names to command aliases. */
	private CaseInsensitiveHashMap<String> aliasMap;

	/**
	 * Default constructor.
	 */
	public EngineConsoleManager()
	{
		commandTrie = new CaseInsensitiveTrie();
		commandMap = new CaseInsensitiveHashMap<CCMDMapping>();
		variableMap = new CaseInsensitiveHashMap<CVARMapping>();
		aliasMap = new CaseInsensitiveHashMap<String>();
	}
	
	/**
	 * Adds the entries for commands and variables to the console manager.
	 */
	public void addEntries(Object instance)
	{
		Class<?> type = instance.getClass();
		TypeProfile<?> profile = TypeProfile.getTypeProfile(type);
		
		// add commands.
		for (Method method : type.getMethods())
		{
			EngineCCMD anno = null;
			if ((anno = method.getAnnotation(EngineCCMD.class)) == null)
				continue;
			
			String cmdname = (Common.isEmpty(anno.value()) ? method.getName().toLowerCase() : anno.value()).toLowerCase();

			if (commandMap.containsKey(cmdname))
			{
				CCMDMapping declaring = commandMap.get(cmdname);
				throw new ConsoleSetupException("Command \""+cmdname+"\" already declared by "+declaring.method.toGenericString());
			}
			
			commandMap.put(cmdname, new CCMDMapping(instance, method, anno.description(), anno.usage()));
			commandTrie.put(cmdname);
		}

		// add variables.
		for (Field field : profile.getAnnotatedPublicFields(EngineCVAR.class))
		{
			EngineCVAR anno = field.getAnnotation(EngineCVAR.class);
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
			
			variableMap.put(varname, new CVARMapping(instance, anno.description(), anno.archived(), field));
		}
		
		for (ObjectPair<String, MethodSignature> pair : profile.getGetterMethods())
		{
			String getterName = pair.getKey();
			MethodSignature signature = pair.getValue();
			Method method = signature.getMethod();

			EngineCVAR anno = method.getAnnotation(EngineCVAR.class);
			String varname = (Common.isEmpty(anno.value()) ? getterName : anno.value()).toLowerCase();

			if (variableMap.containsKey(varname))
			{
				CVARMapping declaring = variableMap.get(varname);
				if (declaring.field != null)
					throw new ConsoleSetupException("Variable \""+varname+"\" already declared by field "+declaring.field.toGenericString());
				else if (declaring.getter != null)
					throw new ConsoleSetupException("Variable \""+varname+"\" already declared by getter "+declaring.getter.toGenericString());
			}
			
			variableMap.put(varname, new CVARMapping(instance, anno.description(), anno.archived(), method));
		}
		
		for (ObjectPair<String, MethodSignature> pair : profile.getSetterMethods())
		{
			String setterName = pair.getKey();
			MethodSignature signature = pair.getValue();
			Method method = signature.getMethod();

			EngineCVAR anno = method.getAnnotation(EngineCVAR.class);
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
	
	/**
	 * Checks if a command alias exists.
	 * @param name the name of the command.
	 * @return true if it exists, false if not.
	 */
	public boolean containsAlias(String name)
	{
		return aliasMap.containsKey(name);
	}

	/**
	 * Returns all alias names in an array.
	 */
	public String[] getAliasNames()
	{
		String[] out = new String[aliasMap.size()];
		Iterator<String> it = aliasMap.keyIterator();
		int i = 0;
		while (it.hasNext())
			out[i++] = it.next();
		return out;
	}

	/**
	 * Gets a command alias by name.
	 * @param name the name of the command.
	 */
	public String getAlias(String name)
	{
		return aliasMap.get(name);
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
			if (args.length < types.length)
				throw new ConsoleCommandInvocationException("Not enough arguments for command.");
			
			Object[] params = new Object[types.length];
			for (int i = 0; i < params.length; i++)
				params[i] = Reflect.createForType(args[i], types[i]);
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
		
		/** Field to change. */
		Field field;
		/** Getter method to call. */
		Method getter;
		/** Setter method to call. */
		Method setter;
		
		/** Type to set. */
		Class<?> type;
		
		CVARMapping(Object instance, String descripton, boolean archived, Field field)
		{
			this.instance = instance;
			this.description = descripton;
			this.archived = archived;
			this.field = field;
			type = field.getType();
		}
		
		CVARMapping(Object instance, String descripton, boolean archived, Method getter)
		{
			this.instance = instance;
			this.description = descripton;
			this.archived = archived;
			this.getter = getter;
			this.setter = null;
			type = field.getType();
		}

		CVARMapping(Object instance, String descripton, boolean archived, Method getter, Method setter)
		{
			this.instance = instance;
			this.description = descripton;
			this.archived = archived;
			this.getter = getter;
			this.setter = setter;
			type = field.getType();
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
