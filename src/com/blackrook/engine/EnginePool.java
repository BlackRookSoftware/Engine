package com.blackrook.engine;

import java.lang.reflect.Constructor;

import com.blackrook.engine.components.EnginePoolable;
import com.blackrook.engine.exception.EnginePoolUnavailableException;

/**
 * Implementation of a pool of reusable objects.
 * @author Matthew Tropiano
 * @param <P> an EnginePoolable object that is also treated like an engine component.
 */
public class EnginePool<P extends EnginePoolable>
{
	/**
	 * Describes the policy to use when procuring available objects and manipulating the pool. 
	 */
	public enum PoolPolicy
	{
		/**
		 * Conservative policy.
		 * <p>
		 * On next object, find first inactive, then oldest expendable. 
		 * If neither are found, throw an {@link EnginePoolUnavailableException}.
		 */
		CONSERVATIVE,
		
		/**
		 * Sensible policy. This is the default policy.
		 * <p>
		 * On next object, find first inactive, then oldest expendable. 
		 * If neither are found, expand the pool and return the first new one.
		 */
		SENSIBLE,
		
		/**
		 * Lenient policy.
		 * <p>
		 * On next object, find first inactive. 
		 * If not found, expand the pool and return the first new one.
		 * This skips the "expendable" check!
		 */
		LENIENT,
		;
		
	}

	/** Constant for doubling pool expansion. */
	public static final int EXPAND_DOUBLE = 0;
	
	/** Reference to engine. */
	private Engine engine;
	
	/** The object class. */
	private Class<P> poolClass; 
	/** The class constructor to use to create each object. Can be null for default. */
	private Constructor<P> constructor;
	/** The pool's manipulation policy. */
	private PoolPolicy policy;
	/** Expansion amount. */
	private int expansionAmount;

	/** The pool itself. */
	private EnginePoolable[] pool;
	
	/** Last search index for available objects. */
	private int searchIndex;
	
	
	/**
	 * Creates a new engine pool. Expands by doubling. Sensible policy.
	 * @param engine the engine instance to grab singletons from, if any.
	 * @param poolClass the class to pool.
	 * @param constructor the constructor to use to create the pool. Can be null for default constructor.
	 * @param startAmount the starting amount in the pool (must be 1 or greater).
	 */
	EnginePool(Engine engine, Class<P> poolClass, Constructor<P> constructor, int startAmount)
	{
		this(engine, poolClass, constructor, PoolPolicy.SENSIBLE, startAmount, EXPAND_DOUBLE);
	}

	/**
	 * Creates a new engine pool. Expands by doubling.
	 * @param engine the engine instance to grab singletons from, if any.
	 * @param poolClass the class to pool.
	 * @param constructor the constructor to use to create the pool. Can be null for default constructor.
	 * @param policy the {@link PoolPolicy} to use.
	 * @param startAmount the starting amount in the pool (must be 1 or greater).
	 */
	EnginePool(Engine engine, Class<P> poolClass, Constructor<P> constructor, PoolPolicy policy, int startAmount)
	{
		this(engine, poolClass, constructor, policy, startAmount, EXPAND_DOUBLE);
	}

	/**
	 * Creates a new engine pool.
	 * @param engine the engine instance to grab singletons from, if any.
	 * @param poolClass the class to pool.
	 * @param constructor the constructor to use to create the pool. Can be null for default constructor.
	 * @param policy the {@link PoolPolicy} to use.
	 * @param startAmount the starting amount in the pool (must be 1 or greater).
	 * @param expansionAmount amount to expand pool by. if 0 or less, it is doubled.
	 */
	EnginePool(Engine engine, Class<P> poolClass, Constructor<P> constructor, PoolPolicy policy, int startAmount, int expansionAmount)
	{
		this.engine = engine;
		this.poolClass = poolClass;
		this.constructor = constructor;
		this.policy = policy;
		this.expansionAmount = expansionAmount;
		
		if (startAmount < 1)
			throw new IllegalArgumentException("starting amount can't be less than 1.");
		
		this.pool = new EnginePoolable[startAmount];
		
		for (int i = 0; i < startAmount; i++)
			this.pool[i] = engine.createComponent(poolClass, constructor);
	}
	
	/**
	 * Returns the next available object, according to policy.
	 * If the object that is about to be returned is both an active and expendable one, {@link EnginePoolable#deactivate()}
	 * will be called on it before it is returned.
	 * @throws EnginePoolUnavailableException if none are found and this pool does not expand (by policy).
	 */
	public synchronized P getAvailable()
	{
		switch (policy)
		{
			case CONSERVATIVE:
			{
				P out = searchForAvailable(true);
				if (out == null)
					throw new EnginePoolUnavailableException("Cannot find an available object for pool of "+poolClass.getSimpleName());
				else if (out.isActive() && out.isExpendable())
					out.deactivate();
				
				return out;
			}
			default:
			case SENSIBLE:
			{
				P out = searchForAvailable(true);
				if (out == null)
				{
					searchIndex = pool.length;
					expand();
					return getAvailable();
				}
				else if (out.isActive() && out.isExpendable())
					out.deactivate();
				
				return out;
			}
			case LENIENT:
			{
				P out = searchForAvailable(false);
				if (out == null)
				{
					searchIndex = pool.length;
					expand();
					return getAvailable();
				}
				return out;
			}
			
		}
		
	}

	/**
	 * Expands this pool.
	 */
	protected void expand()
	{
		int newlen = expansionAmount <= 0 ? pool.length * 2 : pool.length + expansionAmount;
		EnginePoolable[] newpool = new EnginePoolable[newlen];
		System.arraycopy(pool, 0, newpool, 0, pool.length);
		for (int i = pool.length; i < newlen; i++)
			newpool[i] = engine.createComponent(poolClass, constructor);
		
		this.pool = newpool;
	}
	
	/**
	 * Searches for the next available object in the pool.
	 * @param considerExpendable
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private P searchForAvailable(boolean considerExpendable)
	{
		int currentIndex = searchIndex;
		long age = Long.MIN_VALUE;
		int oldestIndex = -1;

		int searchCount = 0;
		P found = null;
		while (found == null && searchCount < pool.length)
		{
			P p = (P)pool[currentIndex]; // get current
			
			if (!p.isActive())
				found = p;
			else if (considerExpendable && p.isExpendable() && p.getAge() > age)
				oldestIndex = currentIndex;
			
			currentIndex = (currentIndex + 1) % pool.length; // advance for next
			searchCount++;
		}
		if (found == null)
			 found = oldestIndex >= 0 ? (P)pool[oldestIndex] : null;

		searchIndex = currentIndex;
		return found;
	}
	
	
}
