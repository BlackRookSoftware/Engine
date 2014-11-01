package com.blackrook.engine.test;

import java.io.File;

import com.blackrook.engine.Engine;
import com.blackrook.engine.EngineConfig;
import com.blackrook.engine.components.EngineConsoleManager;

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
			public File getConsoleLogFile()
			{
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getResourceDefinitionFile()
			{
				// TODO Auto-generated method stub
				return null;
			}
		});
		
		e.getComponent(EngineConsoleManager.class).callCommand("debug", "butt");
		
	}

}
