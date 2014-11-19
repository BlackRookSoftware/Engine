package com.blackrook.engine.test;

import com.blackrook.archetext.annotation.ATName;
import com.blackrook.engine.annotation.ResourceComponent;
import com.blackrook.engine.annotation.Indexed;
import com.blackrook.engine.components.EngineResource;

@ResourceComponent
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
