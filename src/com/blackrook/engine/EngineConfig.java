package com.blackrook.engine;

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
	public String getApplicationPackageRoot();
	
}
