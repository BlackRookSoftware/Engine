package com.blackrook.engine;


import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.blackrook.commons.Common;
import com.blackrook.commons.ObjectPair;
import com.blackrook.commons.Reflect;
import com.blackrook.commons.TypeProfile;
import com.blackrook.commons.TypeProfile.MethodSignature;
import com.blackrook.commons.hash.Hash;
import com.blackrook.commons.hash.HashMap;
import com.blackrook.commons.hash.HashedQueueMap;
import com.blackrook.commons.linkedlist.Queue;
import com.blackrook.commons.list.List;
import com.blackrook.commons.list.SortedMap;
import com.blackrook.commons.spatialhash.IntervalHash;
import com.blackrook.commons.spatialhash.IntervalHashable;
import com.blackrook.engine.annotation.Indexed;
import com.blackrook.engine.annotation.Interval;
import com.blackrook.engine.annotation.IntervalBound;
import com.blackrook.engine.exception.EngineSetupException;
import com.blackrook.engine.roles.EngineResource;

/**
 * Mapping of name-to-resource and id-to-resource.
 */
public class EngineResourceList<R extends EngineResource>
{
	private static final String CACHE_KEY = Common.getPackagePathForClass(EngineResourceList.class) + "/Cache";
	
	private static final List<Class<?>> NUMERIC_CLASSES = new List<Class<?>>(12) 
	{{
		add(Byte.class);
		add(Byte.TYPE);
		add(Short.class);
		add(Short.TYPE);
		add(Integer.class);
		add(Integer.TYPE);
		add(Long.class);
		add(Long.TYPE);
		add(Float.class);
		add(Float.TYPE);
		add(Double.class);
		add(Double.TYPE);
	}};

	
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
	private Hash<String> intervalNameList;
	private HashMap<String, ValueGetter> intervalMinNameList;
	private HashMap<String, ValueGetter> intervalMaxNameList;
	
	/**
	 * Creates a new EngineResourceList.
	 * @param clazz the resource class.
	 */
	EngineResourceList(Class<R> clazz)
	{
		idMap = new HashMap<String, R>();
		tagHash = new HashedQueueMap<String, R>();
		indexMap = new HashMap<String, SortedMap<Index,Queue<R>>>();
		intervalMap = new HashMap<String, IntervalHash<IntervalRange>>();
		indexNameList = new HashMap<String, ValueGetter>();
		intervalNameList = new Hash<String>();
		intervalMinNameList = new HashMap<String, ValueGetter>();
		intervalMaxNameList = new HashMap<String, ValueGetter>();
		
		TypeProfile<?> profile = TypeProfile.getTypeProfile(clazz);
		
		// indices
		for (Field field : profile.getAnnotatedPublicFields(Indexed.class))
		{
			Indexed anno = field.getAnnotation(Indexed.class);
			String name = Common.isEmpty(anno.value()) ? field.getName() : anno.value();

			if (!NUMERIC_CLASSES.contains(field.getType()))
				throw new EngineSetupException("On class "+clazz.getSimpleName()+", Indexed field \""+name+"\" must return a numeric type.");
			if (indexNameList.containsKey(name))
				throw new EngineSetupException("On class "+clazz.getSimpleName()+", Index \""+name+"\" is already declared.");
				
			indexNameList.put(name, new ValueGetter(field));
		}
		for (MethodSignature methodSignature : profile.getAnnotatedGetters(Indexed.class))
		{
			Method method = methodSignature.getMethod();
			Indexed anno = method.getAnnotation(Indexed.class);
			String name = Common.isEmpty(anno.value()) ? Reflect.getFieldName(method.getName()) : anno.value();
			
			if (!NUMERIC_CLASSES.contains(methodSignature.getType()))
				throw new EngineSetupException("On class "+clazz.getSimpleName()+", Indexed getter \""+name+"\" must return a numeric type.");
			if (indexNameList.containsKey(name))
				throw new EngineSetupException("On class "+clazz.getSimpleName()+", Index \""+name+"\" is already declared.");
			
			indexNameList.put(name, new ValueGetter(method));
		}
		
		// intervals
		for (Field field : profile.getAnnotatedPublicFields(Interval.class))
		{
			Interval anno = field.getAnnotation(Interval.class);
			IntervalBound bound = anno.bound();
			String name = Common.isEmpty(anno.value()) ? field.getName() : anno.value();

			if (!NUMERIC_CLASSES.contains(field.getType()))
				throw new EngineSetupException("On class "+clazz.getSimpleName()+", Interval field \""+name+"\" must return a numeric type.");

			intervalNameList.put(name);
			
			if (bound == IntervalBound.MIN)
			{
				if (intervalMinNameList.containsKey(name))
					throw new EngineSetupException("On class "+clazz.getSimpleName()+", Interval \""+name+"\", minimum bound, is already declared.");
				intervalMinNameList.put(name, new ValueGetter(field));
			}
			else if (bound == IntervalBound.MAX)
			{
				if (intervalMaxNameList.containsKey(name))
					throw new EngineSetupException("On class "+clazz.getSimpleName()+", Interval \""+name+"\", maximum bound, is already declared.");
				intervalMaxNameList.put(name, new ValueGetter(field));
			}
				
		}
		for (MethodSignature methodSignature : profile.getAnnotatedGetters(Interval.class))
		{
			Method method = methodSignature.getMethod();
			Interval anno = method.getAnnotation(Interval.class);
			IntervalBound bound = anno.bound();
			String name = Common.isEmpty(anno.value()) ? Reflect.getFieldName(method.getName()) : anno.value();

			if (!NUMERIC_CLASSES.contains(methodSignature.getType()))
				throw new EngineSetupException("On class "+clazz.getSimpleName()+", Interval field \""+name+"\" must return a numeric type.");

			intervalNameList.put(name);
			
			if (bound == IntervalBound.MIN)
			{
				if (intervalMinNameList.containsKey(name))
					throw new EngineSetupException("On class "+clazz.getSimpleName()+", Interval \""+name+"\", minimum bound, is already declared.");
				intervalMinNameList.put(name, new ValueGetter(method));
			}
			else if (bound == IntervalBound.MAX)
			{
				if (intervalMaxNameList.containsKey(name))
					throw new EngineSetupException("On class "+clazz.getSimpleName()+", Interval \""+name+"\", maximum bound, is already declared.");
				intervalMaxNameList.put(name, new ValueGetter(method));
			}
			
		}
		
		// check interval completeness.
		for (String name : intervalNameList)
		{
			if (!intervalMinNameList.containsKey(name) && intervalMaxNameList.containsKey(name))
				throw new EngineSetupException("On class "+clazz.getSimpleName()+", Interval \""+ name + "\", minimum bound is MISSING!");
			if (intervalMinNameList.containsKey(name) && !intervalMaxNameList.containsKey(name))
				throw new EngineSetupException("On class "+clazz.getSimpleName()+", Interval \""+ name + "\", maximum bound is MISSING!");
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

		// add indexed fields.
		reIndex(resource);
		
	}

	// indexes a resource.
	private void reIndex(R resource)
	{
		for (ObjectPair<String, ValueGetter> pair : indexNameList)
		{
			SortedMap<Index, Queue<R>> map = indexMap.get(pair.getKey());
			if (map == null)
			{
				map = new SortedMap<Index, Queue<R>>();
				indexMap.put(pair.getKey(), map);
			}
			
			Index index = new Index(pair.getValue().get(resource));
			Queue<R> rq = map.get(index);
			if (rq == null)
			{
				rq = new Queue<R>();
				map.add(index, rq);
			}
			
			rq.enqueue(resource);
		}
		
		for (String name : intervalNameList)
		{
			IntervalHash<IntervalRange> hash = intervalMap.get(name);
			if (hash == null)
			{
				hash = new IntervalHash<IntervalRange>(10);
				intervalMap.put(name, hash);
			}
			
			hash.addObject(
				new IntervalRange(
					resource, 
					intervalMinNameList.get(name).get(resource), 
					intervalMaxNameList.get(name).get(resource)
				)
			);
		}
		
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
	 * @throws NullPointerException if the indexName, value, or array provided is {@code null}. 
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
	 * @throws NullPointerException if the indexName, value, or array provided is {@code null}. 
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
	 * @throws NullPointerException if the indexName, value, or array provided is {@code null}. 
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

	/**
	 * Gets all objects that straddles a value in a particular interval index.
	 * @param indexName the interval index name.
	 * @param value the value to use as an intersection point.
	 * @param out the output array to put the objects into.
	 * @return the amount of objects returned, up to the size of the provided array.
	 * @throws NullPointerException if the indexName, value, or array provided is {@code null}. 
	 */
	@SuppressWarnings("unchecked")
	public int getIntervalIntersection(String indexName, Number value, R[] out)
	{
		IntervalHash<IntervalRange> hash = intervalMap.get(indexName);
		if (hash == null)
			return 0;
		
		Cache cache = getIntervalCache();
		int amt = hash.getIntersections(value.doubleValue(), (List<IntervalRange>)cache.intervalObjects, true);
		int i = 0;
		for (i = 0; i < amt && i < out.length; i++)
			out[i++] = (R)cache.intervalObjects.getByIndex(i).object;
		
		return i;
	}
	
	/**
	 * Gets all objects that overlaps a value range in a particular interval index.
	 * @param indexName the interval index name.
	 * @param valueMin the minimum value to use as an intersection point.
	 * @param valueMax the maximum value to use as an intersection point.
	 * @param out the output array to put the objects into.
	 * @return the amount of objects returned, up to the size of the provided array.
	 * @throws NullPointerException if the indexName, value, or array provided is {@code null}. 
	 */
	@SuppressWarnings("unchecked")
	public int getIntervalIntersection(String indexName, Number valueMin, Number valueMax, R[] out)
	{
		IntervalHash<IntervalRange> hash = intervalMap.get(indexName);
		if (hash == null)
			return 0;
		
		Cache cache = getIntervalCache();
		int amt = hash.getIntersections(valueMin.doubleValue(), valueMax.doubleValue(), (List<IntervalRange>)cache.intervalObjects, true);
		int i = 0;
		for (i = 0; i < amt && i < out.length; i++)
			out[i++] = (R)cache.intervalObjects.getByIndex(i).object;
		
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
	
	/** Returns the cache used for repeat interval queries. */
	private Cache getIntervalCache()
	{
		Cache out = null;
		if ((out = (Cache)Common.getLocal(CACHE_KEY)) == null)
		{
			out = new Cache();
			Common.setLocal(CACHE_KEY, out);
		}
		return out;
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
		public boolean equals(Object obj)
		{
			if (obj instanceof Index)
				return equals((Index)obj);
			return super.equals(obj);
		}
		
		public boolean equals(Index obj)
		{
			return compareTo(obj) == 0;
		}

		@Override
		public int compareTo(Index index)
		{
			double v1 = value.doubleValue();
			double v2 = index.value.doubleValue();
			if (Double.isNaN(v1))
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
	public static class IntervalRange implements IntervalHashable
	{
		/** The object. */
		private Object object;
		private double center;
		private double halfwidth;
		
		IntervalRange(Object object, Number min, Number max)
		{
			this.object = object;
			double m = min.doubleValue();
			center = m + max.doubleValue() / 2.0;
			halfwidth = center - m;
		}

		
		public Object getObject()
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
	
	/** Reused cache per thread. */
	private static class Cache
	{
		List<IntervalRange> intervalObjects;
		
		Cache()
		{
			intervalObjects = new List<IntervalRange>(100);
		}
	}


}