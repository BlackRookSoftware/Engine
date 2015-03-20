package com.blackrook.engine.test;

import com.blackrook.engine.annotation.component.Component;

@Component
public class TestFailingComponent
{
	public TestFailingComponent()
	{
		//throw new RuntimeException("Intentional FAILURE!");
	}
	
}
