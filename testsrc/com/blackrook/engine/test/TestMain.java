package com.blackrook.engine.test;

import com.blackrook.commons.logging.Logger;
import com.blackrook.engine.annotation.EngineComponent;
import com.blackrook.engine.annotation.EngineComponentConstructor;
import com.blackrook.engine.roles.EngineStarter;

@EngineComponent
public class TestMain implements EngineStarter
{
	private Logger logger;

	@EngineComponentConstructor
	public TestMain(Logger logger)
	{
		this.logger = logger;
	}
	
	
	@Override
	public void start()
	{
		logger.info("Main invoked!");
	}
	
}
