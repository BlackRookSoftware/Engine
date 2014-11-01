package com.blackrook.engine.test;

import com.blackrook.engine.annotation.CCMD;
import com.blackrook.engine.annotation.Component;
import com.blackrook.engine.annotation.ComponentConstructor;

@Component
public class SecondTestComponent
{
	@ComponentConstructor
	public SecondTestComponent(TestComponent test)
	{
		System.out.println(test);
	}
	
	@CCMD(value = "debug2", description = "A debug description.", usage = {"some string"})
	public void debug(String name)
	{
		System.out.println(name);
	}
}
