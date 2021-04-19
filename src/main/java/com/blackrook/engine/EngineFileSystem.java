/*******************************************************************************
 * Copyright (c) 2016-2021 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.ZipException;

import com.blackrook.engine.EngineLoggingFactory.Logger;
import com.blackrook.engine.exception.EngineSetupException;
import com.blackrook.engine.struct.OSUtils;
import com.blackrook.engine.struct.Utils;
import com.blackrook.fs.FSFileArchive;
import com.blackrook.fs.FileSystem;
import com.blackrook.fs.archive.FolderArchive;
import com.blackrook.fs.archive.ZipArchive;

/**
 * Main file system tree for Engine applications. 
 * @author Matthew Tropiano
 */
public class EngineFileSystem extends FileSystem
{
	/** File system logger. */
	private Logger logger;
	/** Config ref. */
	private EngineConfig config;

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
		
		String[] archives = config.getFileSystemArchives();
		String[] stack = config.getFileSystemStack();
		
		if (!Utils.isEmpty(archives))
			includeArchiveList(archives);
		else if (!Utils.isEmpty(stack))
			includeArchiveSet(stack, config.getFileSystemStackArchiveAutoloadExtension());
	}

	/**
	 * Includes a set of archives.
	 * @param archiveFiles the list of paths.
	 */
	private void includeArchiveList(String[] archiveFiles)
	{
		boolean error = false;
		for (String arch : archiveFiles) 
			error = !addArchive(new File(arch)) || error;
			
		if (error)
			throw new EngineSetupException("FileSystem: One or more found archives could not be added!");
	}
	
	/**
	 * Includes a set of archives.
	 * @param fileSystemStack the filesystem path stack.
	 * @param extension the extension to search for.
	 */
	private void includeArchiveSet(final String[] fileSystemStack, final String extension)
	{
		FileFilter packFilter;
		
		if (Utils.isEmpty(extension))
			packFilter = null;
		else
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
					return name.equalsIgnoreCase(extension);
				}
			};
		}
		
		for (String dirPaths : fileSystemStack)
		{
			File dir = new File(dirPaths);
			
			if (!dir.exists())
				throw new EngineSetupException("FileSystem: \""+dir.getPath()+"\" does not exist.");
			else if (!dir.isDirectory())
				throw new EngineSetupException("FileSystem: \""+dir.getPath()+"\" is not a directory.");
			
			if (packFilter != null)
			{
				File[] archiveFiles = dir.listFiles(packFilter);
				
				// Sort lexicographically.
				Arrays.sort(archiveFiles);

				boolean error = false;
				for (File arch : archiveFiles) 
					error = !addArchive(arch) || error;
					
				if (error)
					throw new EngineSetupException("FileSystem: One or more found archives could not be added!");
			}
			
			pushArchive(new FolderArchive(dir));
		}

	}
	
	/**
	 * Attempts to add an archive.
	 * @param file the input archive file.
	 * @return true if successful.
	 */
	private boolean addArchive(File file)
	{
		try {
			pushArchive(new ZipArchive(file));
		} catch (FileNotFoundException e) {
			logger.error(e, "FileSystem: \""+file.getPath()+"\" cannot be found.");
			return false;
		} catch (ZipException e) {
			logger.error(e, "FileSystem: \""+file.getPath()+"\" is not an archive.");
			return false;
		} catch (IOException e) {
			logger.error(e, "FileSystem: \""+file.getPath()+"\" cannot be read.");
			return false;
		} catch (SecurityException e) {
			logger.error(e, "FileSystem: No permission to access \""+file.getPath()+"\".");
			return false;
		}
		
		return true;
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
		if (!Utils.createPathForFile(fullPath))
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
		if (!Utils.createPathForFile(fullPath))
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
			basePath = OSUtils.getWorkingDirectoryPath();
		basePath = basePath.endsWith(File.separator) || basePath.endsWith("/") ? basePath : basePath + File.separator; 
		return basePath + filePath;
	}

}
