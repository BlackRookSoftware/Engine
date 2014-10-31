package com.blackrook.engine.exception;

/**
 * Exception thrown when a console command cannot be completed or
 * an exception occurs during a console command invocation.
 * @author Matthew Tropiano
 */
public class ConsoleCommandInvocationException extends RuntimeException
{
	private static final long serialVersionUID = -5876829518553810250L;

	public ConsoleCommandInvocationException()
	{
		super();
	}

	public ConsoleCommandInvocationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ConsoleCommandInvocationException(String message)
	{
		super(message);
	}

	public ConsoleCommandInvocationException(Throwable cause)
	{
		super(cause);
	}
	
}
