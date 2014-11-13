package com.blackrook.engine.exception;

/**
 * Exception thrown when an object is not available in a pool for initialization.
 * @author Matthew Tropiano
 */
public class EnginePoolUnavailableException extends RuntimeException
{
	private static final long serialVersionUID = 4178316096329498240L;

	public EnginePoolUnavailableException()
	{
		super();
	}

	public EnginePoolUnavailableException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public EnginePoolUnavailableException(String message)
	{
		super(message);
	}

	public EnginePoolUnavailableException(Throwable cause)
	{
		super(cause);
	}
	
}
