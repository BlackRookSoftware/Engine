package com.blackrook.engine.test;

import com.blackrook.engine.annotation.EngineCCMD;
import com.blackrook.engine.annotation.EngineComponent;

@EngineComponent
public class SecondTestComponent
{
	@EngineCCMD(value = "debug2", description = "A debug description.", usage = {"some string"})
	public void debug(String name)
	{
		System.out.println(name);
	}
}
