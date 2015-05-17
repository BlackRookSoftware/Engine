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

}
