package com.blackrook.engine.struct;

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
	
}
