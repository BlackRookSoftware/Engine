package com.blackrook.engine.struct;

import java.util.Arrays;

import com.blackrook.commons.Common;

/**
 * Message type for engine message broadcasting.
 * @author Matthew Tropiano
 */
public class EngineMessage
{
	/** Message type. */
	private String type;
	/** Message arguments. */
	private Object[] arguments;
	
	/** Creates a new message. */
	private EngineMessage(String type, Object ... arguments)
	{
		this.type = type;
		this.arguments = arguments;
	}
	
	/**
	 * Creates a new Engine message.
	 * @param type the message type name.
	 * @param arguments the arguments to pass along with the message.
	 */
	public static EngineMessage create(String type, Object ... arguments)
	{
		return new EngineMessage(type, arguments);
	}
	
	/**
	 * Gets the message type.
	 */
	public String getType() 
	{
		return type;
	}

	/**
	 * Gets the message arguments.
	 */
	public Object[] getArguments()
	{
		return arguments;
	}
	
	/**
	 * Returns a message argument as an Integer.
	 * Will attempt to cast/covert an argument if not an integer.
	 * Returns null if index is outside range or value is null.
	 * @param index the argument index.
	 */
	public boolean getBooleanArgument(int index)
	{
		if (index < 0 || index >= arguments.length)
			return false;
		if (arguments[index] != null)
		{
			Object obj = arguments[index];
			if (obj instanceof Number)
				return (((Number)obj).doubleValue()) != 0.0;
			else if (obj instanceof Boolean)
				return ((Boolean)obj).booleanValue();
			else if (obj instanceof String)
				return Common.parseBoolean((String)obj);
		}
		return false;
	}

	/**
	 * Returns a message argument as an Integer.
	 * Will attempt to cast/covert an argument if not an integer.
	 * Returns null if index is outside range or value is null.
	 * @param index the argument index.
	 */
	public Integer getIntegerArgument(int index)
	{
		if (index < 0 || index >= arguments.length)
			return null;
		if (arguments[index] != null)
		{
			Object obj = arguments[index];
			if (obj instanceof Number)
				return new Integer(((Number)obj).intValue());
			else if (obj instanceof String)
			{
				Integer out = null;
				try {out = Integer.parseInt((String)obj);} catch (NumberFormatException e) {}
				return out;
			}
		}
		return null;
	}

	/**
	 * Returns a message argument as a Long.
	 * Will attempt to cast/covert an argument if not an integer.
	 * Returns null if index is outside range or value is null.
	 * @param index the argument index.
	 */
	public Long getLongIntegerArgument(int index)
	{
		if (index < 0 || index >= arguments.length)
			return null;
		if (arguments[index] != null)
		{
			Object obj = arguments[index];
			if (obj instanceof Number)
				return new Long(((Number)obj).intValue());
			else if (obj instanceof String)
			{
				Long out = null;
				try {out = Long.parseLong((String)obj);} catch (NumberFormatException e) {}
				return out;
			}
		}
		return null;
	}

	/**
	 * Returns a message argument as an int.
	 * Will attempt to cast/covert an argument if not an integer.
	 * Returns def if index is outside range or value is null.
	 * @param index the argument index.
	 */
	public int getIntArgument(int index, int def)
	{
		Integer out = getIntegerArgument(index);
		return out != null ? out.intValue() : def;
	}
	
	/**
	 * Returns a message argument as a long.
	 * Will attempt to cast/covert an argument if not a long.
	 * Returns def if index is outside range or value is null.
	 * @param index the argument index.
	 */
	public long getLongArgument(int index, int def)
	{
		Long out = getLongIntegerArgument(index);
		return out != null ? out.longValue() : def;
	}
	
	/**
	 * Returns a message argument as a String.
	 * Will attempt to cast/covert an argument if not a String.
	 * Returns null if index is outside range or value is null.
	 * @param index the argument index.
	 */
	public String getStringArgument(int index)
	{
		if (index < 0 || index >= arguments.length)
			return null;
		if (arguments[index] != null)
			return String.valueOf(arguments[index]);
		else
			return null;
	}

	/**
	 * Returns a message argument as a String.
	 * Will attempt to cast/covert an argument if not a String.
	 * Returns def if index is outside range or value is null.
	 * @param index the argument index.
	 */
	public String getStringArgument(int index, String def)
	{
		String out = getStringArgument(index);
		return out != null ? out : def;
	}
	
	@Override
	public String toString()
	{
		return type + " " + Arrays.toString(arguments);
	}
	
}
