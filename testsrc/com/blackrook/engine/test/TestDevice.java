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
