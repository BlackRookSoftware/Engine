package com.blackrook.engine.test;

import java.util.Arrays;

import com.blackrook.engine.annotation.Element;
import com.blackrook.engine.annotation.ElementConstructor;
import com.blackrook.engine.annotation.element.CCMD;
import com.blackrook.engine.annotation.element.CVAR;

@Element
public class TestComponent
{
	@CVAR
	public int buttvar;
	
	@ElementConstructor
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
