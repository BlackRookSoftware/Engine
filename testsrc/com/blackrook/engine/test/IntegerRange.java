package com.blackrook.engine.test;

import com.blackrook.archetext.annotation.ATName;
import com.blackrook.engine.annotation.resource.Interval;
import com.blackrook.engine.annotation.resource.IntervalBound;
import com.blackrook.engine.roles.EngineResource;

public class IntegerRange implements EngineResource
{
	private String id;
	
	@Interval(value = "value", bound = IntervalBound.MIN)
	public int min;
	@Interval(value = "value", bound = IntervalBound.MAX)
	public int max;
	
	public IntegerRange()
	{
		id = null;
		min = Integer.MIN_VALUE;
		max = Integer.MAX_VALUE;
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
