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
	 * Returns the e-mail address used for support.
	 */
	public String getApplicationSupportEmail();

	/**
	 * Returns the e-mail address used for general contact.
	 */
	public String getApplicationContactEmail();

	/**
	 * Returns the URL for the application homepage.
	 */
	public String getApplicationHomepage();

	/**
	 * Returns the e-mail address used for general contact.
	 */
	public String getApplicationSupportPage();

	/**
	 * Returns the package root name for scanning stuff in the engine application. 
	 */
	public String[] getPackageRoot();

	/**
	 * Returns the list of singleton/pool classes that should be instantiated on startup.
	 * Their dependencies are also loaded.
	 * All names do not need full qualification.
	 * If null, all classes are loaded.  
	 */
	public String[] getStartupComponentClasses();

	/**
	 * Returns the directory path for storing global configuration.
	 * NOTE: This is OUTSIDE of the file system path.  
	 */
	public String getGlobalSettingsPath();

	/**
	 * Returns the directory path for storing user configuration.
	 * NOTE: This is OUTSIDE of the file system path.  
	 */
	public String getUserSettingsPath();

	/**
	 * Returns the file off of the global directory path for storing/reading archived variables.
	 * If null, saves/reads nothing.
	 * @see #getGlobalSettingsPath()
	 */
	public String getGlobalVariablesFile();

	/**
	 * Returns the file off of the user directory path for storing/reading archived variables.
	 * If null, saves/reads nothing.
	 * @see #getUserSettingsPath()
	 */
	public String getUserVariablesFile();

	/**
	 * Returns the file to log console output to (other than the console itself).
	 * NOTE: This is OUTSIDE of the file system path.  
	 */
	public String getLogFile();

	/**
	 * Returns the logging level for the log.
	 */
	public LogLevel getLogLevel();

	/**
	 * Returns the file system extension for archive systems.
	 * If null, does not add archive types.
	 */
	public String getFileSystemArchiveExtension();

	/**
	 * Returns the list of directory paths to add in lowest to greatest stack precedence.
	 * If null, these directories are not added.
	 */
	public String[] getFileSystemStack();

	/**
	 * Returns the file to read for reading resource definitions. This is filesystem-relative. 
	 * File system is scanned for all of the files and are read additively from lowest to highest precedence. 
	 */
	public String getResourceDefinitionFile();
	
	/**
	 * Returns how many updates per second that the main updating thread needs to do.
	 * If the returned value is 0 or less, this runs full bore. 
	 */
	public int getUpdatesPerSecond();
	
	/**
	 * Returns if this Engine should start in debug mode. 
	 */
	public boolean getDebugMode();

	/**
	 * Returns a list of console commands to execute after startup. 
	 */
	public String[] getConsoleCommandsToExecute();

}
