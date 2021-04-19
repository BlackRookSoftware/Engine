/*******************************************************************************
 * Copyright (c) 2016-2021 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.test;

import com.blackrook.engine.annotation.EngineElement;

@EngineElement
public class TestFailingComponent
{
	public TestFailingComponent()
	{
		//throw new RuntimeException("Intentional FAILURE!");
	}
	
}
