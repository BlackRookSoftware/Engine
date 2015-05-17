package com.blackrook.engine.test;

import com.blackrook.commons.logging.Logger;
import com.blackrook.engine.annotation.Element;
import com.blackrook.engine.annotation.ElementConstructor;
import com.blackrook.engine.roles.EngineDevice;

@Element
public class TestDevice implements EngineDevice
{
	private boolean active;
	private Logger logger;
	
	@ElementConstructor
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
