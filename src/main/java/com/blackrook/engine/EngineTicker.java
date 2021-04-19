/*******************************************************************************
 * Copyright (c) 2016-2021 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine;

import java.util.ArrayList;
import java.util.List;

import com.blackrook.engine.EngineLoggingFactory.Logger;
import com.blackrook.engine.roles.EngineUpdateListener;
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

	private int updatesPerSecond;
	private Thread updateThread;
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
		this.updatesPerSecond = config.getUpdatesPerSecond();
		this.updateThread = null;
		this.enabled = true;
		this.active = false;
	}

	/**
	 * Starts the updater.
	 */
	public void start()
	{
		logger.info("Update ticker starting...");
		if (updateThread == null)
			(updateThread = new Updater()).start();
	}
	
	/**
	 * Stops the updater.
	 */
	public void stop()
	{
		// Thread will die after the last update.
		this.active = false;
	}
	
	/**
	 * Enters the update loop. 
	 */
	private void loop()
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
		
		logger.info("Update ticker stopped.");
		updateThread = null;
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
	
	/**
	 * The updater thread.
	 */
	private class Updater extends Thread
	{
		private Updater()
		{
			setName("EngineUpdater");
			setDaemon(true);
		}
		
		@Override
		public void run()
		{
			logger.info("Update ticker started.");
			active = true;
			loop();
		}
	}
	
}
