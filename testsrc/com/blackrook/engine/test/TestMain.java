package com.blackrook.engine.test;

import com.blackrook.commons.logging.Logger;
import com.blackrook.engine.annotation.component.Component;
import com.blackrook.engine.annotation.component.ComponentConstructor;
import com.blackrook.engine.roles.EngineStart;

@Component
public class TestMain implements EngineStart
{
	private Logger logger;

	@ComponentConstructor
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
