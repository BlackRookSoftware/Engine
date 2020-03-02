/*******************************************************************************
 * Copyright (c) 2016-2020 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.roles;

import com.blackrook.engine.annotation.element.Ordering;
import com.blackrook.engine.handler.EngineInputHandler;

/**
 * This interface describes an object that should receive input events.
 * <p>
 * The Engine uses something called <b>input codes</b> to define actions taken so that the component
 * that is supposed to listen for device actions can broadcast filtered events, or automate them.
 * <p>
 * Input codes should be defined by the layer that reads device input.
 * A good strategy for handling heterogeneous input systems like GUIs are to have those handle system input,
 * and then if the input wasn't handled, pass it along to the Engine.
 * <p>
 * The {@link Ordering} annotation can influence invocation order on this type of object.
 * @author Matthew Tropiano
 */
public interface EngineInputListener extends EngineInputHandler
{

}
