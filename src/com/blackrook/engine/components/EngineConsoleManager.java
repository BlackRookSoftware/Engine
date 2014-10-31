package com.blackrook.engine.components;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.blackrook.commons.hash.CaseInsensitiveHashMap;
import com.blackrook.engine.annotation.EngineComponent;

/**
 * 
 * @author Matthew Tropiano
 */
@EngineComponent
public class EngineConsoleManager
{
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
		commandMap = new CaseInsensitiveHashMap<CCMDMapping>();
		variableMap = new CaseInsensitiveHashMap<CVARMapping>();
		aliasMap = new CaseInsensitiveHashMap<String>();
	}
	
	/**
	 * Adds the entries for commands and variables to the console manager.
	 */
	public void addEntries(Object instance)
	{
		// TODO: Finish.
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

		void call(Object ... args)
		{
			// TODO: Cast/convert and invoke. 
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
			// TODO: Cast/convert and invoke.
			return null;
		}

		Object set(Object value)
		{
			// TODO: Cast/convert and invoke.
			return null;
		}
		
	}
	
}
