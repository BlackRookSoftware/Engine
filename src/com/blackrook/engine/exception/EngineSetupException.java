package com.blackrook.engine.exception;

/**
 * Exception thrown when a problem happens during engine component creation.
 * @author Matthew Tropiano
 */
public class EngineSetupException extends RuntimeException
{
	private static final long serialVersionUID = 4178316096329498240L;

	public EngineSetupException()
	{
		super();
	}

	public EngineSetupException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public EngineSetupException(String message)
	{
		super(message);
	}

	public EngineSetupException(Throwable cause)
	{
		super(cause);
	}
	
}
