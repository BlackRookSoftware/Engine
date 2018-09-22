/*******************************************************************************
 * Copyright (c) 2016-2018 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
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
