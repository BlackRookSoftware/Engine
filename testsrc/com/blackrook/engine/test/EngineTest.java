package com.blackrook.engine.test;

import java.awt.Image;
import java.util.Arrays;

import com.blackrook.engine.Engine;
import com.blackrook.engine.EngineConfig;

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
				return "resources.def";
			}

			@Override
			public String getApplicationName() {
				return "Test";
			}

			@Override
			public String getApplicationVersion() {
				return "0.9";
			}

			@Override
			public Image getApplicationIcon() {
				return null;
			}

			@Override
			public String getFileSystemArchiveExtension() {
				return null;
			}

			@Override
			public String[] getFileSystemStack() {
				return new String[]{"base"};
			}

			@Override
			public String getSettingsPath() {
				return null;
			}

			@Override
			public String getLogFilePath() {
				return "test.log";
			}

			@Override
			public boolean getDebugMode()
			{
				return true;
			}
		});
		
	}

}
