package com.blackrook.engine;

import java.io.File;

/**
 * Configuration for the Engine, which declares basic principles for its application setup.
 * @author Matthew Tropiano
 */
public interface EngineConfig
{
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
