package com.blackrook.engine.test;

import java.awt.Image;

import com.blackrook.engine.Engine;
import com.blackrook.engine.EngineConfig;

public final class EngineTest
{
	public static void main(String[] args)
	{
		new Engine(new EngineConfig()
		{
			@Override
			public String getPackageRoot()
			{
				return "com.blackrook.engine.test";
			}

			@Override
			public String getResourceDefinitionFile()
			{
				// TODO Auto-generated method stub
				return null;
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
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String[] getFileSystemStack() {
				return null; //new String[]{Common.WORK_DIR};
			}

			@Override
			public String getSettingsPath() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getLogFilePath() {
				// TODO Auto-generated method stub
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
