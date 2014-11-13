package com.blackrook.engine.test;

import com.blackrook.engine.Engine;
import com.blackrook.engine.annotation.EngineCCMD;
import com.blackrook.engine.annotation.EngineComponent;
import com.blackrook.engine.annotation.EngineComponentConstructor;

@EngineComponent
public class SecondTestComponent
{
	@EngineComponentConstructor
	public SecondTestComponent(Engine engine, TestComponent test)
	{
		System.out.println(engine+" "+test);
	}
	
	@EngineCCMD(value = "debug2", description = "A debug description.", usage = {"some string"})
	public void debug(String name)
	{
		System.out.println(name);
	}
}
