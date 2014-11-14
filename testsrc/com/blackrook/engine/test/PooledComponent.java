package com.blackrook.engine.test;

import com.blackrook.engine.annotation.EnginePooledComponent;
import com.blackrook.engine.components.EnginePoolable;
import com.blackrook.engine.EnginePool.PoolPolicy;

@EnginePooledComponent(value = 10, policy = PoolPolicy.SENSIBLE)
public class PooledComponent implements EnginePoolable
{
	private boolean active;
	private boolean expendable;
	private long time;
	
	public PooledComponent()
	{
		reset();
	}

	public void reset()
	{
		this.active = false;
		this.expendable = false;
		this.time = 0L;
	}

	public void init(boolean expend)
	{
		time = System.currentTimeMillis();
		expendable = expend;
		active = true;
	}
	
	@Override
	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	@Override
	public boolean isExpendable()
	{
		return expendable;
	}

	@Override
	public long getAge()
	{
		return System.currentTimeMillis() - time;
	}

	@Override
	public void deactivate()
	{
		reset();
	}
	
}
