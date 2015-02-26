package com.blackrook.engine.roles;

import com.blackrook.engine.struct.UpdateHandler;

/**
 * Defines an engine component that is added to the main engine ticker.
 * Most of the time, this will be used on devices.
 * <p>
 * You cannot guarantee the order in which these objects have their methods called on them.
 * @author Matthew Tropiano
 */
public interface EngineUpdatable extends UpdateHandler
{
	
}
