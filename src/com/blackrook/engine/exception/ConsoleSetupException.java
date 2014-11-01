package com.blackrook.engine.exception;

/**
 * Exception thrown when a console command or variable cannot be added for some reason.
 * @author Matthew Tropiano
 */
public class ConsoleSetupException extends RuntimeException
{
	private static final long serialVersionUID = 4178316096329498240L;

	public ConsoleSetupException()
	{
		super();
	}

	public ConsoleSetupException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ConsoleSetupException(String message)
	{
		super(message);
	}

	public ConsoleSetupException(Throwable cause)
	{
		super(cause);
	}
	
}
