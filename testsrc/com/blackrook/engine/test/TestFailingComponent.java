package com.blackrook.engine.test;

import com.blackrook.engine.annotation.EngineElement;

@EngineElement
public class TestFailingComponent
{
	public TestFailingComponent()
	{
		//throw new RuntimeException("Intentional FAILURE!");
	}
	
}
