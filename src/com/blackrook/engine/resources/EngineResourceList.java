package com.blackrook.engine.resources;


import com.blackrook.commons.hash.HashMap;
import com.blackrook.commons.hash.HashedQueueMap;

/**
 * Mapping of name-to-resource and id-to-resource.
 */
public class EngineResourceList<R extends EngineResource>
{
	/** Name-to-id mapping. */
	private HashMap<String, R> idMap;
	/** Tag to id mapping. */
	private HashedQueueMap<String, R> tagHash;
	
	/**
	 * Creates a new EList.
	 * @param resClass the resource class.
	 */
	public EngineResourceList(Class<R> resClass)
	{
		idMap = new HashMap<String, R>();
		tagHash = new HashedQueueMap<String, R>();
	}
	
	/**
	 * Adds a resource to the list.
	 * @param resource the added resource.
	 */
	public synchronized void add(R resource)
	{
		idMap.put(resource.getId(), resource);
		
		for (String s : resource.getTags())
			tagHash.enqueue(s, resource);
		
	}

	/**
	 * Gets a resource by its identity.
	 * Returns null if not found.
	 */
	public R get(String id)
	{
		return idMap.get(id);
	}

	/**
	 * Gets a resource by its identity.
	 * Returns null if not found.
	 */
	public R getByTag(String id)
	{
		return idMap.get(id);
	}

	/**
	 * Clears this list of contents.
	 */
	public void clear()
	{
		idMap.clear();
		tagHash.clear();
	}

}
