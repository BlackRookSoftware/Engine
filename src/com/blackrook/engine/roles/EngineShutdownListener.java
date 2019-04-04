/*******************************************************************************
 * Copyright (c) 2016-2019 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.roles;

import com.blackrook.engine.annotation.element.Ordering;

/**
 * This is a component that has a method invoked on it when the engine is shutting down.
 * These are called after the update ticker is killed.
 * <p>
 * The {@link Ordering} annotation can influence invocation order on this type of object.
 * @author Matthew Tropiano
 */
public interface EngineShutdownListener
{
	/**
	 * Called by Engine when it is shutting down. This is for saving state and
	 * variables and configuration, if necessary, and is called before the
	 * graphics and sound engines get called to shut down the engine, in 
	 * case their state is important.
	 * <p><b>It would not be wise to call this from another method in this class.</b>
	 */
	public void onEngineShutdown();

	/**
	 * Called by Engine when it is shutting down unexpectedly. This is for saving state and
	 * variables and configuration, if necessary, and is called before the
	 * graphics and sound engines get called to shut down the engine, in 
	 * case their state is important.
	 * <p><b>It would not be wise to call this from another method in this class.</b>
	 * @param t the {@link Throwable} that caused the shutdown.
	 */
	public void onUnexpectedEngineShutDown(Throwable t);

}
