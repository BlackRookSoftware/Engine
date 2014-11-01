package com.blackrook.engine.test;

import com.blackrook.engine.annotation.CCMD;
import com.blackrook.engine.annotation.Component;

@Component
public class TestComponent
{
	@CCMD(value = "debug", description = "A debug description.", usage = {"some string"})
	public void debug(String name)
	{
		System.out.println(name);
	}
}
