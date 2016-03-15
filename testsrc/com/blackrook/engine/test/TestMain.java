package com.blackrook.engine.test;

import com.blackrook.commons.logging.Logger;
import com.blackrook.engine.annotation.EngineElement;
import com.blackrook.engine.annotation.EngineElementConstructor;
import com.blackrook.engine.roles.EngineStartupListener;

@EngineElement
public class TestMain implements EngineStartupListener
{
	private Logger logger;

	@EngineElementConstructor
	public TestMain(Logger logger)
	{
		this.logger = logger;
	}
	
	
	@Override
	public void onEngineStartup()
	{
		logger.info("Main invoked!");
	}
	
}
