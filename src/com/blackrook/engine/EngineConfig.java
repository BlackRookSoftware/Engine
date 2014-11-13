package com.blackrook.engine;

import java.awt.Image;
import java.io.File;

/**
 * Configuration for the Engine, which declares basic principles for its application setup.
 * @author Matthew Tropiano
 */
public interface EngineConfig
{
	/**
	 * Returns the application name. 
	 */
	public String getApplicationName();

	/**
	 * Returns the application version string. Can be null.
	 */
	public String getApplicationVersion();

	/**
	 * Returns the icon used for the application. Can be null.
	 */
	public Image getApplicationIcon();

	/**
	 * Returns the package root name for scanning stuff in
	 * the engine application. 
	 */
	public String getPackageRoot();

	/**
	 * Returns the file to log console output to (other than the console itself). 
	 */
	public File getConsoleLogFile();

	/**
	 * Returns the file to read for reading resource definitions.
	 */
	public String getResourceDefinitionFile();
	
}
