package com.blackrook.engine.test;

import java.util.Arrays;

import com.blackrook.engine.annotation.component.CCMD;
import com.blackrook.engine.annotation.component.CVAR;
import com.blackrook.engine.annotation.component.Component;

@Component
public class TestComponent
{
	@CVAR
	public int buttvar;
	
	public TestComponent()
	{
		buttvar = 5;
	}
	
	@CCMD(value = "debug", description = "A debug description.", usage = {"some string"})
	public void debug(String ... name)
	{
		System.out.println(Arrays.toString(name));
	}
	
	
}
