package com.blackrook.engine.exception;

/**
 * Exception thrown when a console variable cannot be set/retrieved or
 * an exception occurs during a set/get.
 * @author Matthew Tropiano
 */
public class ConsoleVariableException extends RuntimeException
{
	private static final long serialVersionUID = 4178316096329498240L;

	public ConsoleVariableException()
	{
		super();
	}

	public ConsoleVariableException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ConsoleVariableException(String message)
	{
		super(message);
	}

	public ConsoleVariableException(Throwable cause)
	{
		super(cause);
	}
	
}
