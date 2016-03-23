/*******************************************************************************
 * Copyright (c) 2016 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine;

import java.awt.Image;

import com.blackrook.commons.logging.LoggingFactory.LogLevel;

/**
 * Configuration for the Engine, which declares basic principles for its application setup.
 * @author Matthew Tropiano
 */
public interface EngineConfig
{
	/**
	 * @return the application name. 
	 */
	public String getApplicationName();

	/**
	 * @return the application version string. Can be null.
	 */
	public String getApplicationVersion();

	/**
	 * @return the icon used for the application. Can be null.
	 */
	public Image getApplicationIcon();

	/**
	 * @return the e-mail address used for support.
	 */
	public String getApplicationSupportEmail();

	/**
	 * @return the e-mail address used for general contact.
	 */
	public String getApplicationContactEmail();

	/**
	 * @return the URL for the application homepage.
	 */
	public String getApplicationHomepage();

	/**
	 * @return the e-mail address used for general contact.
	 */
	public String getApplicationSupportPage();

	/**
	 * @return the package root name for scanning stuff in the engine application. 
	 */
	public String[] getPackageRoot();

	/**
	 * Gets the list of singleton/pool classes that should be instantiated on startup.
	 * Their dependencies are also loaded.
	 * All names do not need full qualification.
	 * If null, all classes are loaded.
	 * @return an array of singleton classes to load.  
	 */
	public String[] getStartupComponentClasses();

	/**
	 * Returns the directory path for storing global configuration.
	 * NOTE: This is OUTSIDE of the file system path.
	 * @return the global settings path root.  
	 */
	public String getGlobalSettingsPath();

	/**
	 * Returns the directory path for storing user configuration.
	 * NOTE: This is OUTSIDE of the file system path.  
	 * @return the user settings path root.  
	 */
	public String getUserSettingsPath();

	/**
	 * Returns the file off of the global directory path for storing/reading archived variables.
	 * If null, saves/reads nothing.
	 * @return the filename to store global variables in.  
	 * @see #getGlobalSettingsPath()
	 */
	public String getGlobalVariablesFile();

	/**
	 * Returns the file off of the user directory path for storing/reading archived variables.
	 * If null, saves/reads nothing.
	 * @return the filename to store user variables in.  
	 * @see #getUserSettingsPath()
	 */
	public String getUserVariablesFile();

	/**
	 * Returns the file to log console output to (other than the console itself).
	 * NOTE: This is OUTSIDE of the file system path.  
	 * @return the filename of the log.  
	 */
	public String getLogFile();

	/**
	 * @return the base logging level for the logging factory.
	 */
	public LogLevel getLogLevel();

	/**
	 * Gets the file system extension for archive types to mount.
	 * Files are considered to be ZIP formatted.
	 * If null, does not add archive types.
	 * @return the archive file extension or null to not load archives.
	 */
	public String getFileSystemArchiveExtension();

	/**
	 * Gets the list of directory paths to add in lowest to greatest stack precedence.
	 * If null, these directories are not added.
	 * @return the paths to push onto the filesystem stack.
	 */
	public String[] getFileSystemStack();

	/**
	 * Gets the file to read for reading resource definitions. This is filesystem-relative. 
	 * File system is scanned for all of the files and are read additively from lowest to highest precedence. 
	 * @return the name of the definition file.
	 */
	public String getResourceDefinitionFile();
	
	/**
	 * Returns how many updates per second that the main updating thread needs to do.
	 * If the returned value is 0 or less, this runs full bore. 
	 * @return the updates per second of the main ticker.
	 */
	public int getUpdatesPerSecond();
	
	/**
	 * @return if this Engine should start in debug mode. 
	 */
	public boolean getDebugMode();

	/**
	 * @return a list of console commands to execute after startup. 
	 */
	public String[] getConsoleCommandsToExecute();

}
