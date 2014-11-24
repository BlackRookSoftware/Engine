package com.blackrook.engine.test;

import com.blackrook.engine.annotation.Component;
import com.blackrook.engine.annotation.Pooled;
import com.blackrook.engine.roles.EnginePoolable;
import com.blackrook.engine.EnginePool.PoolPolicy;

@Component
@Pooled(value = 10, policy = PoolPolicy.SENSIBLE)
public class TestPooledComponent implements EnginePoolable
{
	private boolean active;
	private boolean expendable;
	private long time;
	
	public TestPooledComponent()
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
