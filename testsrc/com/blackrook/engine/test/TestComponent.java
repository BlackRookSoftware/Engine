/*******************************************************************************
 * Copyright (c) 2016-2018 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
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
