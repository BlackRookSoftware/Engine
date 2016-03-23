/*******************************************************************************
 * Copyright (c) 2016 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipException;

import com.blackrook.commons.Common;
import com.blackrook.commons.logging.Logger;
import com.blackrook.engine.exception.EngineSetupException;
import com.blackrook.fs.FSFileArchive;
import com.blackrook.fs.FileSystem;
import com.blackrook.fs.archive.FolderArchive;
import com.blackrook.fs.archive.ZipArchive;

/**
 * Main file system tree for Engine2D applications. 
 * @author Matthew Tropiano
 */
public class EngineFileSystem extends FileSystem
{
	/** File system logger. */
	private Logger logger;
	/** Config ref. */
	private EngineConfig config;

	/** File filter. */
	private FileFilter packFilter;
	
	/**
	 * Creates a new file system.
	 * @param logger the logger.
	 * @param engine the Engine2D instance.
	 * @param config the configuration class to use.
	 */
	EngineFileSystem(Logger logger, Engine engine, EngineConfig config)
	{
		super();
		
		this.config = config;
		this.logger = logger;
		
		final String EXT = config.getFileSystemArchiveExtension();
		packFilter = null;
		if (!Common.isEmpty(config.getFileSystemArchiveExtension()))
		{
			packFilter = new FileFilter() 
			{
				@Override
				public boolean accept(File file)
				{
					if (file.isDirectory())
						return false;
					String name = file.getName();
					name = name.substring(name.lastIndexOf('.')).toLowerCase();
					return name.equalsIgnoreCase(EXT);
				}
			};
		}

		if (!Common.isEmpty(config.getFileSystemStack())) for (String dirPaths : config.getFileSystemStack())
		{
			File dir = new File(dirPaths);
			
			if (!dir.exists())
				throw new EngineSetupException("FileSystem: \""+dir.getPath()+"\" does not exist.");
			else if (!dir.isDirectory())
				throw new EngineSetupException("FileSystem: \""+dir.getPath()+"\" is not a directory.");
			
			if (packFilter != null)
			{
				File[] archiveFiles = dir.listFiles(packFilter);
				try {
					for (File arch : archiveFiles) 
						pushArchive(new ZipArchive(arch));
				} catch (ZipException e) {
					throw new EngineSetupException("FileSystem: \""+dir.getPath()+"\" is not an archive.", e);
				} catch (IOException e) {
					throw new EngineSetupException("FileSystem: \""+dir.getPath()+"\" cannot be read.", e);
				} catch (SecurityException e) {
					throw new EngineSetupException("FileSystem: No permission to access \""+dir.getPath()+"\".", e);
				}
			}
			
			pushArchive(new FolderArchive(dir));
		}
	}
	
	@Override
	public void pushArchive(FSFileArchive fsfa)
	{
		super.pushArchive(fsfa);
		logger.info("Pushed " + fsfa.getPath());
	}

	/**
	 * Creates a new file off of the global settings path provided by {@link EngineConfig}.
	 * If {@link EngineConfig#getGlobalSettingsPath()} returns null, the base path is the current working directory.
	 * @param path the path to use.
	 * @return an open OutputStream for writing to the file, or null if it couldn't be open.
	 * @throws IOException if a write error occurs.
	 * @see EngineConfig#getGlobalSettingsPath()
	 */
	public OutputStream createGlobalSettingFile(String path) throws IOException
	{
		String fullPath = getOutPath(config.getGlobalSettingsPath(), path);
		if (fullPath == null)
			return null;
		logger.infof("Creating global setting path \"%s\"...", fullPath);
		if (!Common.createPathForFile(fullPath))
			return null;
		OutputStream out = new FileOutputStream(fullPath);
		return out;
	}

	/**
	 * Creates a new file off of the user settings path provided by {@link EngineConfig}.
	 * If {@link EngineConfig#getUserSettingsPath()} returns null, the base path is the current working directory.
	 * @param path the path to use.
	 * @return an open OutputStream for writing to the file, or null if it couldn't be open.
	 * @throws IOException if a write error occurs.
	 * @see EngineConfig#getUserSettingsPath()
	 */
	public OutputStream createUserSettingFile(String path) throws IOException
	{
		String fullPath = getOutPath(config.getUserSettingsPath(), path);
		if (fullPath == null)
			return null;
		logger.infof("Creating user setting path \"%s\"...", fullPath);
		if (!Common.createPathForFile(fullPath))
			return null;
		OutputStream out = new FileOutputStream(fullPath);
		return out;
	}

	/**
	 * Creates a new file off of the global settings path provided by {@link EngineConfig}.
	 * If {@link EngineConfig#getGlobalSettingsPath()} returns null, the base path is the current working directory.
	 * @param path the path to use.
	 * @return an open InputStream for reading from the file, or null if the file does not exist.
	 * @throws IOException if a read error occurs.
	 * @see EngineConfig#getGlobalSettingsPath()
	 */
	public InputStream openGlobalSettingFile(String path) throws IOException
	{
		String fullPath = getGlobalSettingFilePath(path);
		if (fullPath == null)
			return null;
		logger.infof("Opening global setting path \"%s\"...", fullPath);
		InputStream out = new FileInputStream(fullPath);
		return out;
	}

	/**
	 * Creates a new file off of the user settings path provided by {@link EngineConfig}.
	 * If {@link EngineConfig#getUserSettingsPath()} returns null, the base path is the current working directory.
	 * @param path the path to use.
	 * @return an open InputStream for reading from the file.
	 * @throws IOException if a read error occurs.
	 * @see EngineConfig#getUserSettingsPath()
	 */
	public InputStream openUserSettingFile(String path) throws IOException
	{
		String fullPath = getUserSettingFilePath(path);
		if (fullPath == null)
			return null;
		logger.infof("Opening user setting path \"%s\"...", fullPath);
		InputStream out = new FileInputStream(fullPath);
		return out;
	}

	/**
	 * Gets the path to a file in the global setting path.
	 * @param path the path off of the path root.
	 * @return the full file path to the file.
	 */
	public String getGlobalSettingFilePath(String path)
	{
		return getOutPath(config.getGlobalSettingsPath(), path);
	}
	
	/**
	 * Gets the path to a file in the user setting path. 
	 * @param path the path off of the path root.
	 * @return the full file path to the file.
	 */
	public String getUserSettingFilePath(String path)
	{
		return getOutPath(config.getUserSettingsPath(), path);
	}
	
	// assembles an out path.
	private String getOutPath(String basePath, String filePath)
	{
		if (filePath == null)
			return null;
		if (basePath == null)
			basePath = Common.WORK_DIR;
		basePath = basePath.endsWith(File.separator) || basePath.endsWith("/") ? basePath : basePath + File.separator; 
		return basePath + filePath;
	}

}
