/*******************************************************************************
 * Copyright (c) 2016-2020 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.adapter;

import java.awt.Image;

import com.blackrook.engine.EngineConfig;
import com.blackrook.engine.struct.LoggingFactory.LogLevel;

/**
 * Adapter class for engine configs.
 * Without overriding, this config does the following:
 * <ul>
 * 	<li>Application name is null.</li>
 * 	<li>Application version is null.</li>
 * 	<li>Application icon is null.</li>
 * 	<li>Application support E-mail is null.</li>
 * 	<li>Application contact E-mail is null.</li>
 * 	<li>Application support homepage is null.</li>
 * 	<li>Application support page is null.</li>
 * 	<li>Package roots is empty.</li>
 * 	<li>Startup component class list is empty.</li>
 * 	<li>Global setting path is empty.</li>
 * 	<li>User setting path is empty.</li>
 * 	<li>Global variables file path is empty.</li>
 * 	<li>User variables file path is empty.</li>
 * 	<li>Log file path is empty.</li>
 * 	<li>Log level is INFO.</li>
 * 	<li>File system stack is empty.</li>
 * 	<li>File system archive list is empty.</li>
 * 	<li>File system archive extension autoload is empty.</li>
 * 	<li>Resource definition file is empty.</li>
 * 	<li>Updates per second is null (no ticker).</li>
 * 	<li>Debug mode is false.</li>
 * 	<li>List of console commands to execute is empty.</li>
 * </ul>
 * @author Matthew Tropiano
 */
public class EngineConfigAdapter implements EngineConfig
{

	@Override
	public String getApplicationName()
	{
		return null;
	}

	@Override
	public String getApplicationVersion()
	{
		return null;
	}

	@Override
	public Image getApplicationIcon()
	{
		return null;
	}

	@Override
	public String getApplicationSupportEmail()
	{
		return null;
	}

	@Override
	public String getApplicationContactEmail()
	{
		return null;
	}

	@Override
	public String getApplicationHomepage()
	{
		return null;
	}

	@Override
	public String getApplicationSupportPage()
	{
		return null;
	}

	@Override
	public String[] getPackageRoots()
	{
		return null;
	}

	@Override
	public String[] getStartupComponentClasses()
	{
		return null;
	}

	@Override
	public String getGlobalSettingsPath()
	{
		return null;
	}

	@Override
	public String getUserSettingsPath()
	{
		return null;
	}

	@Override
	public String getGlobalVariablesFile()
	{
		return null;
	}

	@Override
	public String getUserVariablesFile()
	{
		return null;
	}

	@Override
	public String getLogFile()
	{
		return null;
	}

	@Override
	public LogLevel getLogLevel()
	{
		return null;
	}

	@Override
	public String[] getFileSystemStack()
	{
		return null;
	}

	@Override
	public String[] getFileSystemArchives()
	{
		return null;
	}

	@Override
	public String getFileSystemStackArchiveAutoloadExtension()
	{
		return null;
	}

	@Override
	public String getResourceDefinitionFile()
	{
		return null;
	}

	@Override
	public Integer getUpdatesPerSecond()
	{
		return null;
	}

	@Override
	public boolean getDebugMode()
	{
		return false;
	}

	@Override
	public String[] getConsoleCommandsToExecute()
	{
		return null;
	}

}
