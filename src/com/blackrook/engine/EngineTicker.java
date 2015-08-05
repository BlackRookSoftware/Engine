/*******************************************************************************
 * Copyright (c) 2009-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *  
 * Contributors:
 *     Matt Tropiano - initial API and implementation
 ******************************************************************************/
package com.blackrook.engine;

import com.blackrook.commons.ResettableIterator;
import com.blackrook.commons.Ticker;
import com.blackrook.commons.linkedlist.Queue;
import com.blackrook.commons.logging.Logger;
import com.blackrook.engine.roles.EngineUpdateListener;

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
	/** Update ticker. */
	private Ticker updateTicker;
	
	/** Queue of updatables. */
	private Queue<EngineUpdateListener> updatables;
	/** Queue iterator. */
	private ResettableIterator<EngineUpdateListener> updatableIterator;

	/**
	 * Creates a new engine ticker.
	 * @param engine the Engine2D instance.
	 * @param config the configuration class to use.
	 */
	EngineTicker(Logger logger, Engine engine, EngineConfig config)
	{
		this.logger = logger;
		this.engine = engine;
		updatables = new Queue<EngineUpdateListener>();
		updateTicker = new UpdateTicker(config.getUpdatesPerSecond());
		updatableIterator = updatables.iterator();
	}

	public void start()
	{
		updateTicker.start();
		logger.info("Started Ticker.");
	}
	
	/**
	 * Suspends the ticker.
	 */
	public void pause()
	{
		updateTicker.setSuspended(true);
		logger.info("Ticker suspended.");
	}
	
	/**
	 * Resumes the ticker.
	 */
	public void resume()
	{
		updateTicker.setSuspended(false);
		logger.info("Ticker resumed.");
	}

	/**
	 * Stops the ticker dead.
	 */
	public void stop()
	{
		updateTicker.stop();
		logger.info("Ticker stopped.");
	}
	
	/** Adds an updatable. */
	public void add(EngineUpdateListener updatable)
	{
		updatables.add(updatable);
	}
	
	// updates all attached updatables.
	private void update(long tick, long currentNanos)
	{
		updatableIterator.reset();
		try {
			while (updatableIterator.hasNext())
			{
				updatableIterator.next().update(tick, currentNanos);
			}
		} catch (Throwable t) {
			logger.severe("An exception occurred!");
			engine.handleException(t);
		}
	}

	// Update Ticker
	private class UpdateTicker extends Ticker
	{
		public UpdateTicker(int updatesPerSecond)
		{
			super("UpdateTicker", updatesPerSecond);
		}
		
		@Override
		public void doTick(long tick)
		{
			update(tick, System.nanoTime());
		}
	}
	
}
