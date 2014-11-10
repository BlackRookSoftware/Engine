package com.blackrook.engine.exception;

/**
 * An Exception thrown when a particular type of engine component is desired
 * but is not a valid component.
 * @author Matthew Tropiano
 */
public class NoSuchComponentException extends RuntimeException
{
	private static final long serialVersionUID = 4178316096329498240L;

	public NoSuchComponentException()
	{
		super();
	}

	public NoSuchComponentException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public NoSuchComponentException(String message)
	{
		super(message);
	}

	public NoSuchComponentException(Throwable cause)
	{
		super(cause);
	}
	
}
