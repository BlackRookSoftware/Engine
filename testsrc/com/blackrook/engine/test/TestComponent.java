package com.blackrook.engine.test;

import com.blackrook.engine.annotation.EngineCCMD;
import com.blackrook.engine.annotation.EngineCVAR;
import com.blackrook.engine.annotation.EngineComponent;

@EngineComponent
public class TestComponent
{
	@EngineCVAR
	public int buttvar;
	
	public TestComponent()
	{
		buttvar = 5;
	}
	
	@EngineCCMD(value = "debug", description = "A debug description.", usage = {"some string"})
	public void debug(String name)
	{
		System.out.println(name);
	}
	
	
}
