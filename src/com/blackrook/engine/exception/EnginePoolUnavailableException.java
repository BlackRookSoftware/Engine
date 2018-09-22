/*******************************************************************************
 * Copyright (c) 2016-2018 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
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
