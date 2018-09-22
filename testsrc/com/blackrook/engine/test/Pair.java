/*******************************************************************************
 * Copyright (c) 2016-2018 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.test;

import com.blackrook.archetext.annotation.ATName;
import com.blackrook.engine.annotation.resource.DefinitionName;
import com.blackrook.engine.annotation.resource.Indexed;
import com.blackrook.engine.roles.EngineResource;

@DefinitionName("pair")
public class Pair implements EngineResource
{
	private String id;
	
	@Indexed
	public int x;
	@Indexed
	public int y;
	
	public Pair()
	{
		id = null;
		x = 0;
		y = 0;
	}

	@ATName
	@Override
	public String getId()
	{
		return this.id;
	}
	
	@ATName
	public void setId(String id)
	{
		this.id = id;
	}

	@Override
	public String[] getTags()
	{
		return new String[0];
	}

}
