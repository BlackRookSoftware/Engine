package com.blackrook.engine.test;

import java.util.Arrays;

import com.blackrook.engine.annotation.EngineComponent;
import com.blackrook.engine.annotation.EngineComponentConstructor;
import com.blackrook.engine.annotation.component.CCMD;
import com.blackrook.engine.annotation.component.CVAR;

@EngineComponent
public class TestComponent
{
	@CVAR
	public int buttvar;
	
	@EngineComponentConstructor
	public TestComponent(TestComponentTwo comp)
	{
		buttvar = 5;
	}
	
	@CCMD(value = "debug", description = "A debug description.", usage = {"some string"})
	public void debug(String ... name)
	{
		System.out.println(Arrays.toString(name));
	}
	
	
}
