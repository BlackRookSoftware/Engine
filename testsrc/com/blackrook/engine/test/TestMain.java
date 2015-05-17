package com.blackrook.engine.test;

import com.blackrook.commons.logging.Logger;
import com.blackrook.engine.annotation.Element;
import com.blackrook.engine.annotation.ElementConstructor;
import com.blackrook.engine.roles.EngineStartupListener;

@Element
public class TestMain implements EngineStartupListener
{
	private Logger logger;

	@ElementConstructor
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
