/*******************************************************************************
 * Copyright (c) 2016-2020 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine;

import java.util.ArrayList;
import java.util.List;

import com.blackrook.engine.roles.EngineUpdateListener;
import com.blackrook.engine.struct.LoggingFactory.Logger;
import com.blackrook.engine.struct.Utils;

/**
 * An update thread class that updates all updatable objects on a set interval.
 * @author Matthew Tropiano
 */
public class EngineTicker
{
	/** Ticker logger. */
	private Logger logger;
	/** Engine reference. */
	private Engine engine;
	/** Queue of updatables. */
	private List<EngineUpdateListener> updatables;

	private boolean enabled;
	private boolean active;
	
	/**
	 * Creates a new engine ticker.
	 * @param engine the Engine2D instance.
	 * @param config the configuration class to use.
	 */
	EngineTicker(Logger logger, Engine engine, EngineConfig config)
	{
		this.logger = logger;
		this.engine = engine;
		this.updatables = new ArrayList<EngineUpdateListener>();
		this.enabled = true;
		this.active = true;
	}

	/**
	 * Sets if the loop will update the list of updatables.
	 * @param enabled true to enable, false to disable.
	 */
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	/**
	 * Stops the loop.
	 */
	public void stop()
	{
		this.active = false;
	}
	
	/**
	 * Enters the update loop. 
	 * @param updatesPerSecond the amount of updates per second.
	 */
	void loop(int updatesPerSecond)
	{
		double millisPerUpdate = updatesPerSecond != 0 ? (1000.0 / updatesPerSecond) : 0;
		long millis = (long)millisPerUpdate;
		long nanos = (long)((millisPerUpdate - millis) * 1000000L);
		
		long tick = 0;
		long nanoCount = 0;
		long lastNanos = System.nanoTime();
	
		while (active)
		{
			long totalNanos = millis * 1000000L + nanos;
	
			long nt = System.nanoTime();
			nanoCount += nt - lastNanos;
			lastNanos = nt;
			
			if (totalNanos == 0 || nanoCount >= totalNanos)
			{
				nanoCount -= totalNanos;
				if (enabled)
					update(tick++, System.nanoTime());
			}
			
			Utils.sleep(0, 500000);
		}
	}

	/** 
	 * Adds an updatable to this ticker.
	 * If no updater was created, this does nothing.
	 * @param updatable the EngineUpdateListener to add. 
	 */
	void add(EngineUpdateListener updatable)
	{
		updatables.add(updatable);
	}
	
	/**
	 * Calls a single tick step.
	 * If the the thread is running, do NOT call this method,
	 * as that may add additional updates outside of the timing!
	 */
	private void update(long tick, long currentNanos)
	{
		try {
			for (int i = 0; i < updatables.size(); i++)
				updatables.get(i).update(tick, currentNanos);
		} catch (Throwable t) {
			logger.severe(t, "An exception occurred!");
			engine.handleException(t);
		}
	}
}
