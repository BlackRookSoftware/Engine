package com.blackrook.engine.test;

import com.blackrook.engine.annotation.EngineComponent;

@EngineComponent
public class TestFailingComponent
{
	public TestFailingComponent()
	{
		//throw new RuntimeException("Intentional FAILURE!");
	}
	
}
