package com.blackrook.engine.test;

import com.blackrook.engine.annotation.Element;

@Element
public class TestFailingComponent
{
	public TestFailingComponent()
	{
		//throw new RuntimeException("Intentional FAILURE!");
	}
	
}
