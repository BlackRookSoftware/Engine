/*******************************************************************************
 * Copyright (c) 2016-2021 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.receiver;

import com.blackrook.engine.annotation.EngineElement;
import com.blackrook.engine.roles.EngineWindowBroadcaster;

/**
 * This class is designed to be the class that implementors of {@link EngineWindowBroadcaster}
 * use to fire window events to the engine. 
 * <p>
 * This is because there might be different implementations of windowing toolkits, 
 * and the engine would still need to have a consistent way to hear window events. 
 * <p>
 * Basically, if a {@link EngineElement} is supposed to spawn the main window of the
 * application, it should fire all of its main window events through this object so
 * that it is broadcast to all listening components. 
 * @author Matthew Tropiano 
 */
public interface EngineWindowEventReceiver
{
	/**
	 * Should be called when the window receives a signal to be closed (by OS - usually
	 * is a shortcut like Alt-F4 in Windows or clicking the close button in the corner
	 * of the window in most window systems).
	 */
	public void fireClosing();

	/**
	 * Should be called when the window is minimizing (sometimes called "shrinking" or "iconifying").
	 */
	public void fireMinimize();

	/**
	 * Should be called when the window is restored from minimization.
	 */
	public void fireRestore();

	/**
	 * Should be called when the window receives focus in the desktop.
	 */
	public void fireFocus();

	/**
	 * Should be called when the window loses focus in the desktop for whatever reason.
	 */
	public void fireBlur();

	/**
	 * Should be called when the mouse enters the main canvas of the window.
	 */
	public void fireMouseEnter();

	/**
	 * Should be called when the mouse leaves the main canvas of the window.
	 */
	public void fireMouseExit();

	/**
	 * Should be called when the mouse moves on the main canvas.
	 * @param canvasX the new canvas-local x-coordinate.
	 * @param canvasY the new canvas-local y-coordinate.
	 */
	public void fireMouseMove(int canvasX, int canvasY);

	/**
	 * Should be called when the canvas is resized.
	 * @param width the new canvas width.
	 * @param height the new canvas height.
	 */
	public void fireResize(int width, int height);

	/**
	 * Should be called when the canvas is moved.
	 * @param positionX the new canvas ABSOLUTE POSITION X on the screen.
	 * @param positionY the new canvas ABSOLUTE POSITION Y on the screen.
	 */
	public void fireMove(int positionX, int positionY);

}
