/*******************************************************************************
 * Copyright (c) 2016-2021 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
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
