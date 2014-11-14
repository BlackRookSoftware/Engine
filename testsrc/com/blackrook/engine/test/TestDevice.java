package com.blackrook.engine.test;

import com.blackrook.commons.logging.Logger;
import com.blackrook.engine.annotation.EngineComponent;
import com.blackrook.engine.annotation.EngineComponentConstructor;
import com.blackrook.engine.components.EngineDevice;

@EngineComponent
public class TestDevice implements EngineDevice
{
	private boolean active;
	private Logger logger;
	
	@EngineComponentConstructor
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
