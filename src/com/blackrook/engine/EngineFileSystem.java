package com.blackrook.engine;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
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
	/** Sound system logger. */
	protected Logger logger;
	
	/** File filter. */
	protected FileFilter packFilter;
	
	/**
	 * Creates a new file system.
	 * @param engine the Engine2D instance.
	 * @param config the configuration class to use.
	 */
	EngineFileSystem(Engine engine, EngineConfig config)
	{
		super();
		
		logger = engine.getLogger("FileSystem");
		
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

}
