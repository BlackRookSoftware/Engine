package com.blackrook.engine.test;

import com.blackrook.commons.logging.Logger;
import com.blackrook.engine.annotation.Component;
import com.blackrook.engine.annotation.ComponentConstructor;
import com.blackrook.engine.components.EngineDevice;

@Component
public class TestDevice implements EngineDevice
{
	private boolean active;
	private Logger logger;
	
	@ComponentConstructor
	public TestDevice(Logger logger)
	{
		this.logger = logger;
	}
	
	@Override
	public String getName()
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
