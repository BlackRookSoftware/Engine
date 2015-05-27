package com.blackrook.engine.test;

import com.blackrook.engine.swing.CrashDialog;

public final class ProgramCrashDialogTest
{

	public static void main(String[] args)
	{
		CrashDialog.showDialog("Test", "UH OH", "Something got brokened.", "Close", null);
		System.exit(0);
	}

}
