package com.blackrook.engine.test;

import com.blackrook.engine.Engine;
import com.blackrook.engine.EngineConfig;
import com.blackrook.engine.console.EngineConsoleManager;

public final class EngineTest
{
	public static void main(String[] args)
	{
		Engine e = new Engine(new EngineConfig()
		{
			@Override
			public String getPackageRoot()
			{
				return "com.blackrook.engine.test";
			}

			@Override
			public String getResourceDefinitionFile()
			{
				return null;
			}

			@Override
			public String[] getFilesystemStack()
			{
				return null;
			}

			@Override
			public String getFileSystemArchiveExtension()
			{
				return null;
			}

			@Override
			public String getSettingsPath()
			{
				return null;
			}

			@Override
			public String getLogFilePath()
			{
				return null;
			}
		});
		
		EngineConsoleManager manager = e.getComponent(EngineConsoleManager.class); 
		manager.callCommand("debug", "butt");
		System.out.println(manager.getVariable("buttvar"));
		manager.setVariable("buttvar", 10);
		System.out.println(manager.getVariable("buttvar"));
		
		for (int i = 0; i < 10; i++)
			e.getPooledComponent(PooledComponent.class).init(true);
			
		e.getPooledComponent(PooledComponent.class).init(true);
		
	}

}
