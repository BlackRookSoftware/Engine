/*******************************************************************************
 * Copyright (c) 2016 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.test;

import com.blackrook.commons.logging.Logger;
import com.blackrook.engine.annotation.EngineElement;
import com.blackrook.engine.annotation.EngineElementConstructor;
import com.blackrook.engine.roles.EngineDevice;

@EngineElement
public class TestDevice implements EngineDevice
{
	private boolean active;
	private Logger logger;
	
	@EngineElementConstructor
	public TestDevice(Logger logger)
	{
		this.logger = logger;
	}
	
	@Override
	public String getDeviceName()
	{
		return "TestDevice";
	}

	@Override
	public boolean isActive()
	{
		return active;
	}

	@Override
	public boolean create()
	{
		active = true;
		logger.info("Created.");
		return true;
	}

	@Override
	public boolean destroy()
	{
		active = false;
		logger.info("Destroyed.");
		return true;
	}

}
