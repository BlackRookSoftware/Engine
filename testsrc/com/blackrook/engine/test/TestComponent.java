package com.blackrook.engine.test;

import java.util.Arrays;

import com.blackrook.engine.annotation.EngineElement;
import com.blackrook.engine.annotation.EngineElementConstructor;
import com.blackrook.engine.annotation.element.CCMD;
import com.blackrook.engine.annotation.element.CVAR;

@EngineElement
public class TestComponent
{
	@CVAR
	public int buttvar;
	
	@EngineElementConstructor
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
