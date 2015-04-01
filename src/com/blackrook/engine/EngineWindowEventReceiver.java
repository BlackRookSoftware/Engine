package com.blackrook.engine;

import com.blackrook.engine.annotation.EngineComponent;
import com.blackrook.engine.broadcaster.EngineWindowBroadcaster;

/**
 * This class is designed to be the class that implementors of {@link EngineWindowBroadcaster}
 * use to fire window events to the engine. 
 * <p>
 * This is because there might be different implementations of windowing toolkits, 
 * and the engine would still need to have a consistent way to hear window events. 
 * <p>
 * Basically, if a {@link EngineComponent} is supposed to spawn the main window of the
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

}
