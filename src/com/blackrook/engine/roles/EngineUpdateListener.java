package com.blackrook.engine.roles;

import com.blackrook.engine.annotation.element.Ordering;

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
