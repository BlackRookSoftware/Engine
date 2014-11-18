package com.blackrook.engine.resources;


import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.blackrook.commons.Common;
import com.blackrook.commons.ObjectPair;
import com.blackrook.commons.Reflect;
import com.blackrook.commons.TypeProfile;
import com.blackrook.commons.TypeProfile.MethodSignature;
import com.blackrook.commons.hash.HashMap;
import com.blackrook.commons.hash.HashedQueueMap;
import com.blackrook.commons.linkedlist.Queue;
import com.blackrook.commons.list.SortedMap;
import com.blackrook.commons.spatialhash.IntervalHash;
import com.blackrook.commons.spatialhash.IntervalHashable;
import com.blackrook.engine.annotation.Indexed;
import com.blackrook.engine.annotation.Interval;
import com.blackrook.engine.exception.EngineSetupException;

/**
 * Mapping of name-to-resource and id-to-resource.
 */
public class EngineResourceList<R extends EngineResource>
{
	/** Name-to-id mapping. */
	private HashMap<String, R> idMap;
	/** Tag to id mapping. */
	private HashedQueueMap<String, R> tagHash;
	/** Object index. */
	private HashMap<String, SortedMap<Index, Queue<R>>> indexMap;
	/** Object interval index. */
	private HashMap<String, IntervalHash<IntervalRange>> intervalMap;
	
	/** Index names. */
	private HashMap<String, ValueGetter> indexNameList;
	/** Interval names. */
	private HashMap<String, ValueGetter> intervalNameList;
	
	/**
	 * Creates a new EngineResourceList.
	 * @param clazz the resource class.
	 */
	public EngineResourceList(Class<R> clazz)
	{
		idMap = new HashMap<String, R>();
		tagHash = new HashedQueueMap<String, R>();
		indexMap = new HashMap<String, SortedMap<Index,Queue<R>>>();
		intervalMap = new HashMap<String, IntervalHash<IntervalRange>>();
		indexNameList = new HashMap<String, ValueGetter>();
		
		TypeProfile<?> profile = TypeProfile.getTypeProfile(clazz);
		for (Field field : profile.getAnnotatedPublicFields(Indexed.class))
		{
			Indexed anno = field.getAnnotation(Indexed.class);
			String name = Common.isEmpty(anno.value()) ? field.getName() : anno.value();

			if (!Number.class.isAssignableFrom(field.getType()))
				throw new EngineSetupException("Indexed field \""+name+"\" must return a numeric type.");
			if (indexNameList.containsKey(name))
				throw new EngineSetupException("Index \""+name+"\" is already declared.");
				
			indexNameList.put(name, new ValueGetter(field));
		}
		for (MethodSignature methodSignature : profile.getAnnotatedGetters(Indexed.class))
		{
			Method method = methodSignature.getMethod();
			Indexed anno = method.getAnnotation(Indexed.class);
			String name = Common.isEmpty(anno.value()) ? method.getName() : anno.value();
			
			if (!Number.class.isAssignableFrom(methodSignature.getType()))
				throw new EngineSetupException("Indexed getter \""+name+"\" must return a numeric type.");
			if (indexNameList.containsKey(name))
				throw new EngineSetupException("Index \""+name+"\" is already declared.");
			
			indexNameList.put(name, new ValueGetter(method));
		}
		for (Field field : profile.getAnnotatedPublicFields(Interval.class))
		{
			// TODO: Finish.
		}
		for (MethodSignature methodSignature : profile.getAnnotatedGetters(Indexed.class))
		{
			// TODO: Finish.
		}
		
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
		
		// TODO: Finish.
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
	 * Gets a resource by a tag name.
	 * @param tagName the tag name.
	 * @param out the output array to put the objects into.
	 * @return the amount of objects returned, up to the size of the provided array.
	 * @throws NullPointerException if the tagName or array provided is {@code null}. 
	 */
	public int getByTag(String tagName, R[] out)
	{
		Queue<R> queue = tagHash.get(tagName);
		if (queue == null)
			return 0;
		
		int i = 0;
		for (R object : queue)
		{
			out[i++] = object;
			if (i >= out.length)
				break;
		}
		
		return i;
	}

	/**
	 * Gets all objects that match an index value.
	 * @param indexName the name of the index.
	 * @param value the value to search for.
	 * @param out the output array to put the objects into.
	 * @return the amount of objects returned, up to the size of the provided array.
	 * @throws NullPointerException if the indexValue, value, or array provided is {@code null}. 
	 */
	public int getByIndex(String indexName, Number value, R[] out)
	{
		Index index = new Index(value);
		
		SortedMap<Index, Queue<R>> map = indexMap.get(indexName);
		if (map == null)
			return 0;
		
		Queue<R> queue = map.get(index);
		if (queue == null)
			return 0;
		
		int i = 0;
		for (R object : queue)
		{
			out[i++] = object;
			if (i >= out.length)
				break;
		}
		
		return i;
	}

	/**
	 * Gets all objects that are before an index value.
	 * @param indexName the name of the index.
	 * @param value the value to search for.
	 * @param out the output array to put the objects into.
	 * @return the amount of objects returned, up to the size of the provided array.
	 * @throws NullPointerException if the indexValue, value, or array provided is {@code null}. 
	 */
	public int getBeforeIndex(String indexName, Number value, R[] out)
	{
		Index index = new Index(value);

		SortedMap<Index, Queue<R>> map = indexMap.get(indexName);
		if (map == null)
			return 0;

		int q = closestIndex(map, index, true);
		
		if (q < 0)
			return 0;
		
		int i = 0;
		for (int x = q; x > 0 && i < out.length; x--)
		{
			Queue<R> queue = map.getValueAtIndex(q);
			
			for (R object : queue)
			{
				out[i++] = object;
				if (i >= out.length)
					break;
			}
		}
		
		return i;
	}

	/**
	 * Gets all objects that are after an index value.
	 * @param indexName the name of the index.
	 * @param value the value to search for.
	 * @param out the output array to put the objects into.
	 * @return the amount of objects returned, up to the size of the provided array.
	 * @throws NullPointerException if the indexValue, value, or array provided is {@code null}. 
	 */
	public int getAfterIndex(String indexName, Number value, R[] out)
	{
		Index index = new Index(value);

		SortedMap<Index, Queue<R>> map = indexMap.get(indexName);
		if (map == null)
			return 0;

		int q = closestIndex(map, index, false);
		
		if (q < 0)
			return 0;
		
		int i = 0;
		for (int x = q; x < map.size() && i < out.length; x++)
		{
			Queue<R> queue = map.getValueAtIndex(q);
			
			for (R object : queue)
			{
				out[i++] = object;
				if (i >= out.length)
					break;
			}
		}
		
		return i;
	}

	// Find closest index.
	private static int closestIndex(SortedMap<Index, ?> map, Index index, boolean lower)
	{
		int u = map.size(), l = 0;
		int i = (u+l)/2;
		int prev = u;
		
		while (i != prev)
		{
			if ((map.getByIndex(i)).getKey().equals(index))
				return i;
			
			int c = (map.getByIndex(i)).getKey().compareTo(index); 
			
			if (c < 0)
				l = i;
			else if (c == 0)
				return i;
			else
				u = i;
			
			prev = i;
			i = (u+l)/2;
		}
		
		int c = index.compareTo(map.getByIndex(i).getKey());
		
		if (i == 0 && c < 0)
			return -1;

		if (i == map.size() - 1 && c > 0)
			return -1;
		
		if (c > 0)
			return lower ? i : i + 1;

		if (c < 0)
			return lower ? i - 1 : i;
		
		return -1;
	}
	
	/**
	 * Value getter thing for ease of use. 
	 */
	public static class ValueGetter
	{
		private Field field;
		private Method method;
		
		ValueGetter(Field field)
		{
			this.field = field;
		}
		
		ValueGetter(Method method)
		{
			this.method = method;
		}
		
		public Number get(Object instance)
		{
			if (field != null)
				return (Number)Reflect.getFieldValue(field, instance);
			if (method != null)
				return (Number)Reflect.invokeBlind(method, instance);
			return null;
		}

	}
	
	/**
	 * Index type.
	 */
	public static class Index implements Comparable<Index>
	{
		/** Value in the index. */
		public Number value;
		
		Index(Number value)
		{
			this.value = value;
		}
		
		@Override
		public int compareTo(Index index)
		{
			double v1 = value.doubleValue();
			double v2 = index.value.doubleValue();
			if (Double.isInfinite(v1))
			{
				if (Double.isInfinite(v2))
					return 0;
				else
					return 1;
			}
			else if (Double.isInfinite(v2))
				return -1;
			else if (Double.isNaN(v1))
			{
				if (Double.isNaN(v2))
					return 0;
				else
					return -1;
			}
			else if (Double.isNaN(v2))
				return 1;
			else
				return v1 == v2 ? 0 : v1 < v2 ? -1 : 1;
		}
		
	}
	
	/**
	 * Interval type.
	 */
	public class IntervalRange implements IntervalHashable
	{
		/** The object. */
		private R object;
		private double center;
		private double halfwidth;
		
		IntervalRange(R object, Number min, Number max)
		{
			this.object = object;
			
			double m = min.doubleValue();
			double d = m + max.doubleValue();
			center = d / 2.0;
			halfwidth = center - m;
		}

		
		public R getObject()
		{
			return object;
		}
		
		@Override
		public double getObjectCenterX()
		{
			return center;
		}

		@Override
		public double getObjectHalfWidth()
		{
			return halfwidth;
		}

		@Override
		public double getObjectSweepX()
		{
			return 0.0;
		}
		
	}
	
}
