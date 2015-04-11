package com.blackrook.engine.test;

import java.awt.Image;

import com.blackrook.commons.Common;
import com.blackrook.commons.logging.LoggingFactory.LogLevel;
import com.blackrook.engine.Engine;
import com.blackrook.engine.EngineConfig;
import com.blackrook.engine.EngineResourceList;

public final class EngineTest
{
	public static void main(String[] args)
	{
		Engine engine = new Engine(new EngineConfig()
		{
			@Override
			public String[] getPackageRoot()
			{
				return new String[]{"com.blackrook.engine.test"};
			}

			@Override
			public String[] getStartupComponentClasses()
			{
				return null;
			}
			
			@Override
			public String getResourceDefinitionFile()
			{
				return "resources.def";
			}

			@Override
			public String getApplicationName()
			{
				return "Test";
			}

			@Override
			public String getApplicationVersion()
			{
				return "0.9";
			}

			@Override
			public Image getApplicationIcon() 
			{
				return null;
			}

			@Override
			public String getFileSystemArchiveExtension() 
			{
				return null;
			}

			@Override
			public String[] getFileSystemStack() 
			{
				return new String[]{"base"};
			}

			@Override
			public String getLogFile()
			{
				return "test.log";
			}

			@Override
			public boolean getDebugMode()
			{
				return true;
			}

			@Override
			public int getUpdatesPerSecond()
			{
				return 30;
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
			public LogLevel getLogLevel()
			{
				return LogLevel.DEBUG;
			}

			@Override
			public String[] getConsoleCommandsToExecute()
			{
				return null;
			}
		});
		
		IntegerRange[] out = new IntegerRange[5];
		EngineResourceList<IntegerRange> list = engine.getResourceList(IntegerRange.class);
		list.getIntervalIntersection("value", 8, out);
		Common.noop();
	}

}
