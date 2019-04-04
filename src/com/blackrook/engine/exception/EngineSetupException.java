/*******************************************************************************
 * Copyright (c) 2016-2019 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
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
