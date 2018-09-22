/*******************************************************************************
 * Copyright (c) 2016-2018 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;

import com.blackrook.commons.Common;
import com.blackrook.commons.ObjectPair;
import com.blackrook.commons.Reflect;
import com.blackrook.commons.Sizable;
import com.blackrook.commons.TypeProfile;
import com.blackrook.commons.TypeProfile.MethodSignature;
import com.blackrook.commons.hash.Hash;
import com.blackrook.commons.hash.HashMap;
import com.blackrook.commons.hash.HashedQueueMap;
import com.blackrook.commons.index.SpatialIndex1D;
import com.blackrook.commons.index.SpatialIndex1DModel;
import com.blackrook.commons.linkedlist.Queue;
import com.blackrook.commons.list.List;
import com.blackrook.commons.list.SortedMap;
import com.blackrook.commons.math.Tuple1D;
import com.blackrook.commons.math.geometry.Point1D;
import com.blackrook.engine.annotation.resource.Indexed;
import com.blackrook.engine.annotation.resource.Interval;
import com.blackrook.engine.annotation.resource.IntervalBound;
import com.blackrook.engine.exception.EngineSetupException;
import com.blackrook.engine.exception.NoSuchComponentException;
import com.blackrook.engine.roles.EngineResource;

/**
 * The thing that holds and manages all engine resources (by class).
 * @author Matthew Tropiano
 */
public class EngineResources
{
	/** Engine resources. */
	private HashMap<Class<?>, ResourceSet<?>> resources;

	EngineResources()
	{
		this.resources = new HashMap<>();
	}
	
	@SuppressWarnings("unchecked")
	public <T extends EngineResource> void addResource(T resource)
	{
		Class<T> clazz = (Class<T>)resource.getClass();
		ResourceSet<T> resourceList;
		if (!resources.containsKey(clazz))
			resources.put(clazz, resourceList = new ResourceSet<T>(clazz));
		else 
			resourceList = (ResourceSet<T>)resources.get(clazz);

		resourceList.add(resource);
	}
	
	/**
	 * Returns the resource list that stores a set of resources.
	 * @param <T> the type contained by the list.
	 * @param clazz the resource class to retrieve the list of.
	 * @return the corresponding list of resources.
	 * @throws NoSuchComponentException if the provided class is not a valid resource component.
	 */
	@SuppressWarnings("unchecked")
	public <T extends EngineResource> ResourceSet<T> getResourceSet(Class<T> clazz)
	{
		return (ResourceSet<T>)resources.get(clazz);
	}
	
	/**
	 * Returns the resource list that stores a set of resources.
	 * @param <T> the type contained by the list.
	 * @param clazz the resource class to retrieve the list of.
	 * @param name the name 
	 * @return the corresponding list of resources.
	 * @throws NoSuchComponentException if the provided class is not a valid resource component.
	 */
	public <T extends EngineResource> T getResource(Class<T> clazz, String name)
	{
		ResourceSet<T> list;
		if ((list = getResourceSet(clazz)) == null)
			return null;
		else
			return list.get(name);
	}

	/**
	 * Mapping of name-to-resource and id-to-resource.
	 */
	public static class ResourceSet<R extends EngineResource> implements Iterable<R>, Sizable
	{
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
		
		/** This class. */
		private Class<R> listClass;
		
		/** Name-to-id mapping. */
		private HashMap<String, R> idMap;
		/** Tag to id mapping. */
		private HashedQueueMap<String, R> tagHash;
		/** Object index. */
		private HashMap<String, SortedMap<Index, Queue<R>>> indexMap;
		/** Object interval index. */
		private HashMap<String, IntervalMap> intervalMap;
		
		/** Index names. */
		private HashMap<String, ValueGetter> indexNameList;
		/** Interval names. */
		private Hash<String> intervalNameList;
		/** List for interval construction - min names. */
		private HashMap<String, ValueGetter> intervalMinNameList;
		/** List for interval construction - max names. */
		private HashMap<String, ValueGetter> intervalMaxNameList;
		
		/**
		 * Creates a new EngineResourceList.
		 * @param clazz the resource class.
		 */
		ResourceSet(Class<R> clazz)
		{
			this.listClass = clazz;
			idMap = new HashMap<String, R>();
			tagHash = new HashedQueueMap<String, R>();
			indexMap = new HashMap<String, SortedMap<Index,Queue<R>>>();
			intervalMap = new HashMap<String, IntervalMap>();
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
		public void add(R resource)
		{
			String id = resource.getId();
			if (id == null)
				throw new EngineSetupException("Attempted to add resource of class \""+listClass.getSimpleName()+"\". No id!");
			
			idMap.put(id, resource);
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
				IntervalMap hash = intervalMap.get(name);
				if (hash == null)
				{
					hash = new IntervalMap(10);
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
		 * @param id the identity of thge resource.
		 * @return the resource desired, or null if not found.
		 */
		public R get(String id)
		{
			return idMap.get(id);
		}
	
		/**
		 * Gets resources by a tag name.
		 * The target array provided will be filled with the qualifying objects sequentially
		 * and will stop if the end is reached, even if there may be more to return.
		 * @param tagName the tag name.
		 * @param out the output array to put the objects into.
		 * @return the amount of objects returned, up to the size of the provided array.
		 * @throws NullPointerException if the tagName or array provided is {@code null}. 
		 */
		public int getByTag(String tagName, R[] out)
		{
			return getByTag(tagName, out, 0);
		}
	
		/**
		 * Gets resources by a tag name.
		 * The target array provided will be filled with the qualifying objects sequentially
		 * and will stop if the end is reached, even if there may be more to return.
		 * @param tagName the tag name.
		 * @param out the output array to put the objects into.
		 * @param offset the offset into the array to start putting objects. 
		 * @return the amount of objects returned, up to the size of the provided array.
		 * @throws NullPointerException if the tagName or array provided is {@code null}. 
		 */
		public int getByTag(String tagName, R[] out, int offset)
		{
			Queue<R> queue = tagHash.get(tagName);
			if (queue == null)
				return 0;
	
			int count = 0;
			int i = offset;
			for (R object : queue)
			{
				if (i >= out.length)
					break;
				
				out[i++] = object;
				count++;
			}
			
			return count;
		}
	
		/**
		 * Gets all objects that match an index value.
		 * The target array provided will be filled with the qualifying objects sequentially
		 * and will stop if the end is reached, even if there may be more to return.
		 * @param indexName the name of the index.
		 * @param value the value to search for.
		 * @param out the output array to put the objects into.
		 * @return the amount of objects returned, up to the size of the provided array.
		 * @throws NullPointerException if the indexName, value, or array provided is {@code null}. 
		 */
		public int getByIndex(String indexName, Number value, R[] out)
		{
			return getByIndex(indexName, value, out, 0);
		}
	
		/**
		 * Gets all objects that match an index value.
		 * The target array provided will be filled with the qualifying objects sequentially
		 * and will stop if the end is reached, even if there may be more to return.
		 * @param indexName the name of the index.
		 * @param value the value to search for.
		 * @param out the output array to put the objects into.
		 * @param offset the offset into the array to start putting objects. 
		 * @return the amount of objects returned, up to the size of the provided array.
		 * @throws NullPointerException if the indexName, value, or array provided is {@code null}. 
		 */
		public int getByIndex(String indexName, Number value, R[] out, int offset)
		{
			Index index = new Index(value);
			
			SortedMap<Index, Queue<R>> map = indexMap.get(indexName);
			if (map == null)
				return 0;
			
			Queue<R> queue = map.get(index);
			if (queue == null)
				return 0;
			
			int count = 0;
			int i = offset;
			for (R object : queue)
			{
				if (i >= out.length)
					break;
				
				out[i++] = object;
				count++;
			}
			
			return count;
		}
	
		/**
		 * Gets all objects that are before an index value.
		 * The target array provided will be filled with the qualifying objects sequentially
		 * and will stop if the end is reached, even if there may be more to return.
		 * @param indexName the name of the index.
		 * @param value the value to search for.
		 * @param out the output array to put the objects into.
		 * @return the amount of objects returned, up to the size of the provided array.
		 * @throws NullPointerException if the indexName, value, or array provided is {@code null}. 
		 */
		public int getBeforeIndex(String indexName, Number value, R[] out)
		{
			return getBeforeIndex(indexName, value, out, 0);
		}
	
		/**
		 * Gets all objects that are before an index value.
		 * The target array provided will be filled with the qualifying objects sequentially
		 * and will stop if the end is reached, even if there may be more to return.
		 * @param indexName the name of the index.
		 * @param value the value to search for.
		 * @param out the output array to put the objects into.
		 * @param offset the offset into the array to start putting objects. 
		 * @return the amount of objects returned, up to the size of the provided array.
		 * @throws NullPointerException if the indexName, value, or array provided is {@code null}. 
		 */
		public int getBeforeIndex(String indexName, Number value, R[] out, int offset)
		{
			Index index = new Index(value);
	
			SortedMap<Index, Queue<R>> map = indexMap.get(indexName);
			if (map == null)
				return 0;
	
			int q = closestIndex(map, index, true);
			
			if (q < 0)
				return 0;
	
			int count = 0;
			int i = offset;
			for (int x = q; x > 0 && i < out.length; x--)
			{
				Queue<R> queue = map.getValueAtIndex(q);
				
				for (R object : queue)
				{
					if (i >= out.length)
						break;
					
					out[i++] = object;
					count++;
				}
			}
			
			return count;
		}
	
		/**
		 * Gets all objects that are after an index value.
		 * The target array provided will be filled with the qualifying objects sequentially
		 * and will stop if the end is reached, even if there may be more to return.
		 * @param indexName the name of the index.
		 * @param value the value to search for.
		 * @param out the output array to put the objects into.
		 * @return the amount of objects returned, up to the size of the provided array.
		 * @throws NullPointerException if the indexName, value, or array provided is {@code null}. 
		 */
		public int getAfterIndex(String indexName, Number value, R[] out)
		{
			return getAfterIndex(indexName, value, out, 0);
		}
		
		/**
		 * Gets all objects that are after an index value.
		 * The target array provided will be filled with the qualifying objects sequentially
		 * and will stop if the end is reached, even if there may be more to return.
		 * @param indexName the name of the index.
		 * @param value the value to search for.
		 * @param out the output array to put the objects into.
		 * @param offset the offset into the array to start putting objects. 
		 * @return the amount of objects returned, up to the size of the provided array.
		 * @throws NullPointerException if the indexName, value, or array provided is {@code null}. 
		 */
		public int getAfterIndex(String indexName, Number value, R[] out, int offset)
		{
			Index index = new Index(value);
	
			SortedMap<Index, Queue<R>> map = indexMap.get(indexName);
			if (map == null)
				return 0;
	
			int q = closestIndex(map, index, false);
			
			if (q < 0)
				return 0;
	
			int count = 0;
			int i = offset;
			for (int x = q; x < map.size() && i < out.length; x++)
			{
				Queue<R> queue = map.getValueAtIndex(q);
				
				for (R object : queue)
				{
					if (i >= out.length)
						break;
					
					out[i++] = object;
					count++;
				}
			}
			
			return count;
		}
	
		/**
		 * Gets all objects that straddle a value in a particular interval index.
		 * The target array provided will be filled with the qualifying objects sequentially
		 * and will stop if the end is reached, even if there may be more to return.
		 * @param indexName the interval index name.
		 * @param value the value to use as an intersection point.
		 * @param out the output array to put the objects into.
		 * @return the amount of objects returned, up to the size of the provided array.
		 * @throws NullPointerException if the indexName, value, or array provided is {@code null}. 
		 */
		public int getIntervalIntersection(String indexName, Number value, R[] out)
		{
			return getIntervalIntersection(indexName, value, out, 0);
		}
		
		/**
		 * Gets all objects that straddle a value in a particular interval index.
		 * The target array provided will be filled with the qualifying objects sequentially
		 * and will stop if the end is reached, even if there may be more to return.
		 * @param indexName the interval index name.
		 * @param value the value to use as an intersection point.
		 * @param out the output array to put the objects into.
		 * @param offset the offset into the array to start putting objects. 
		 * @return the amount of objects returned, up to the size of the provided array.
		 * @throws NullPointerException if the indexName, value, or array provided is {@code null}. 
		 */
		@SuppressWarnings("unchecked")
		public int getIntervalIntersection(String indexName, Number value, R[] out, int offset)
		{
			IntervalMap hash = intervalMap.get(indexName);
			if (hash == null)
				return 0;
			
			Cache cache = getCache();
			int amt = hash.getIntersections(value.doubleValue(), cache.intervalObjects, offset);
			int count = 0;
			for (int i = offset; i < amt && i < out.length; i++)
			{
				out[i] = (R)cache.intervalObjects.getByIndex(i).object;
				count++;
			}
			
			return count;
		}
		
		/**
		 * Gets all objects that overlap a value range in a particular interval index.
		 * The target array provided will be filled with the qualifying objects sequentially
		 * and will stop if the end is reached, even if there may be more to return.
		 * @param indexName the interval index name.
		 * @param valueMin the minimum value to use as an intersection point.
		 * @param valueMax the maximum value to use as an intersection point.
		 * @param out the output array to put the objects into.
		 * @return the amount of objects returned, up to the size of the provided array.
		 * @throws NullPointerException if the indexName, value, or array provided is {@code null}. 
		 */
		public int getIntervalIntersection(String indexName, Number valueMin, Number valueMax, R[] out)
		{
			return getIntervalIntersection(indexName, valueMin, valueMax, out, 0);
		}
	
		/**
		 * Gets all objects that overlap a value range in a particular interval index.
		 * The target array provided will be filled with the qualifying objects sequentially
		 * and will stop if the end is reached, even if there may be more to return.
		 * @param indexName the interval index name.
		 * @param valueMin the minimum value to use as an intersection point.
		 * @param valueMax the maximum value to use as an intersection point.
		 * @param out the output array to put the objects into.
		 * @param offset the offset into the array to start putting objects. 
		 * @return the amount of objects returned, up to the size of the provided array.
		 * @throws NullPointerException if the indexName, value, or array provided is {@code null}. 
		 */
		@SuppressWarnings("unchecked")
		public int getIntervalIntersection(String indexName, Number valueMin, Number valueMax, R[] out, int offset)
		{
			IntervalMap hash = intervalMap.get(indexName);
			if (hash == null)
				return 0;
			
			Cache cache = getCache();
			int amt = hash.getIntersections(valueMin.doubleValue(), valueMax.doubleValue(), cache.intervalObjects, offset);
			int count = 0;
			for (int i = offset; i < amt && i < out.length; i++)
			{
				out[i++] = (R)cache.intervalObjects.getByIndex(i).object;
				count++;
			}
			
			return count;
		}
		
		@Override
		public Iterator<R> iterator()
		{
			return idMap.valueIterator();
		}
	
		@Override
		public int size()
		{
			return idMap.size();
		}
		
		@Override
		public boolean isEmpty()
		{
			return idMap.isEmpty();
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
		public static class IntervalRange
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
			
			public double getCenter()
			{
				return center;
			}
			
			public double getHalfwidth()
			{
				return halfwidth;
			}
		}
		
		/* Built-in interval map. */
		private class IntervalMap extends SpatialIndex1D<IntervalRange>
		{
			public IntervalMap(int resolution)
			{
				super(new IntervalModel(), resolution);
			}
		}
		
		private class IntervalModel implements SpatialIndex1DModel<IntervalRange>
		{
	
			@Override
			public void getCenter(IntervalRange object, Point1D point)
			{
				point.x = object.center;
			}
	
			@Override
			public void getHalfWidths(IntervalRange object, Tuple1D halfwidths)
			{
				halfwidths.x = object.halfwidth;
			}
			
		}
		
		private static final String CACHE_NAME = "$$"+Cache.class.getCanonicalName();
	
		// Get the cache.
		private static Cache getCache()
		{
			Cache out;
			if ((out = (Cache)Common.getLocal(CACHE_NAME)) == null)
				Common.setLocal(CACHE_NAME, out = new Cache());
			return out;
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
	
}
