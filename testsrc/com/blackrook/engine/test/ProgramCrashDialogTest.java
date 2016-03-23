/*******************************************************************************
 * Copyright (c) 2016 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
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
