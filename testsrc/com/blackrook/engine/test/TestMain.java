/*******************************************************************************
 * Copyright (c) 2016-2018 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.test;

import com.blackrook.commons.logging.Logger;
import com.blackrook.engine.annotation.EngineElement;
import com.blackrook.engine.annotation.EngineElementConstructor;
import com.blackrook.engine.roles.EngineReadyListener;

@EngineElement
public class TestMain implements EngineReadyListener
{
	private Logger logger;

	@EngineElementConstructor
	public TestMain(Logger logger)
	{
		this.logger = logger;
	}
	
	
	@Override
	public void onEngineReady()
	{
		logger.info("Main invoked!");
	}
	
}
