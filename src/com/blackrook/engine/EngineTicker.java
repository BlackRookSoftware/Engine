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
import com.blackrook.engine.roles.EngineUpdatable;

/**
 * An update thread class that updates all updatable objects on a set interval.
 * TODO: Add mechanism for adding the {@link EngineUpdatable} objects.
 * @author Matthew Tropiano
 */
public class EngineTicker
{
	/** Ticker logger. */
	private Logger logger;
	/** Update ticker. */
	private Ticker updateTicker;
	
	/** Queue of updatables. */
	private Queue<EngineUpdatable> updatables;
	/** Queue iterator. */
	private ResettableIterator<EngineUpdatable> updatableIterator;

	/**
	 * Creates a new engine ticker.
	 * @param engine the Engine2D instance.
	 * @param config the configuration class to use.
	 */
	public EngineTicker(Engine engine, EngineConfig config)
	{
		logger = engine.getLogger("Ticker");
		updatables = new Queue<EngineUpdatable>();
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
	
	// updates all attached updatables.
	private void update(long tick, long currentNanos)
	{
		// TODO: Finish.
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
