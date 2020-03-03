/*******************************************************************************
 * Copyright (c) 2016-2020 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.roles;

import com.blackrook.engine.annotation.element.Ordering;
import com.blackrook.engine.handler.EngineUpdateHandler;

/**
 * Defines an engine component that is added to the main engine ticker.
 * Most of the time, this will be used on devices.
 * <p>
 * The {@link Ordering} annotation can influence invocation order on this type of object.
 * @author Matthew Tropiano
 */
public interface EngineUpdateListener extends EngineUpdateHandler
{
	
}
