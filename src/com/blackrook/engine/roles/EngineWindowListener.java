/*******************************************************************************
 * Copyright (c) 2016 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.roles;

import com.blackrook.engine.annotation.element.Ordering;

/**
 * Describes a component that is automatically added to a listener group that
 * has its functions called whenever important engine things happen.
 * <p>
 * The {@link Ordering} annotation can influence invocation order on this type of object.
 * @author Matthew Tropiano
 */
public interface EngineWindowListener
{

	/**
	 * Called by Engine when it detects that the main window is getting closed.
	 * <p><b>It would not be wise to call this from another method in this class.</b>
	 */
	public void onClosing();

	/**
	 * Called by Engine when it detects that the main window is getting minimized.
	 * <p><b>It would not be wise to call this from another method in this class.</b>
	 */
	public void onMinimize();

	/**
	 * Called by Engine when it detects that the main window is getting restored from a minimize.
	 * <p><b>It would not be wise to call this from another method in this class.</b>
	 */
	public void onRestore();

	/**
	 * Called by Engine when it detects that the main window gains focus by the OS.
	 * <p><b>It would not be wise to call this from another method in this class.</b>
	 */
	public void onFocus();

	/**
	 * Called by Engine when it detects that the main window loses focus by the OS.
	 * <p><b>It would not be wise to call this from another method in this class.</b>
	 */
	public void onBlur();

	/**
	 * Called by Engine when the mouse has entered the main display canvas.
	 * <p><b>It would not be wise to call this from another method in this class.</b>
	 */
	public void onMouseEnter();

	/**
	 * Called by Engine when the mouse has exited the main display canvas.
	 * <p><b>It would not be wise to call this from another method in this class.</b>
	 */
	public void onMouseExit();

	/**
	 * Called by Engine when the mouse has moved on the main display canvas.
	 * <p><b>It would not be wise to call this from another method in this class.</b>
	 */
	public void onMouseMove(int canvasX, int canvasY);

	/**
	 * Should be called when the canvas is resized.
	 * <p><b>It would not be wise to call this from another method in this class.</b>
	 */
	public void onResize(int width, int height);

	/**
	 * Should be called when the canvas is moved (absolutely).
	 * <p><b>It would not be wise to call this from another method in this class.</b>
	 */
	public void onMove(int positionX, int positionY);

}
