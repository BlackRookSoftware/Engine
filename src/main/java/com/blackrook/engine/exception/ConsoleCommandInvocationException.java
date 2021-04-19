/*******************************************************************************
 * Copyright (c) 2016-2021 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
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
