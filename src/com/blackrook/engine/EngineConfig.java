package com.blackrook.engine;

import java.awt.Image;

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
	 * Returns the package root name for scanning stuff in the engine application. 
	 */
	public String getPackageRoot();

	/**
	 * Returns the file system extension for archive systems.
	 * If null, does not add archive types.
	 */
	public String getFileSystemArchiveExtension();

	/**
	 * Returns the list of directory paths to add in lowest to greatest stack precedence.
	 * If null, these directories are not added.
	 */
	public String[] getFilesystemStack();

	/**
	 * Returns the directory path for storing user configuration. 
	 * This is added to the top of the filestack automatically.
	 */
	public String getSettingsPath();

	/**
	 * Returns the file to log console output to (other than the console itself).
	 * This is filesystem-relative: it gets written to the topmost writable directory. 
	 */
	public String getLogFilePath();

	/**
	 * Returns the file to read for reading resource definitions. This is filesystem-relative. 
	 * File system is scanned for all of the files and are read additively from lowest to highest precedence. 
	 */
	public String getResourceDefinitionFile();
	
}
