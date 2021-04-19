/*******************************************************************************
 * Copyright (c) 2016-2021 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.blackrook.engine.annotation.resource.Indexed;
import com.blackrook.engine.exception.EngineSetupException;
import com.blackrook.engine.struct.HashDequeMap;
import com.blackrook.engine.struct.TreeDequeMap;
import com.blackrook.engine.struct.TypeProfileFactory.Profile;
import com.blackrook.engine.struct.TypeProfileFactory.Profile.FieldInfo;
import com.blackrook.engine.struct.TypeProfileFactory.Profile.MethodInfo;
import com.blackrook.engine.struct.Utils;

/**
 * The thing that holds and manages all engine resources (by class).
 * @author Matthew Tropiano
 */
public class EngineResourceSet
{
	/** Engine resources. */
	private Map<Class<?>, ResourceSet<?>> resources;

	EngineResourceSet()
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
	 * @return the corresponding list of resources, or null if no corresponding set.
	 */
	@SuppressWarnings("unchecked")
	public <T extends EngineResource> ResourceSet<T> getResourceSet(Class<T> clazz)
	{
		return (ResourceSet<T>)resources.get(clazz);
	}
	
	/**
	 * Returns a resource of a particular type and id.
	 * @param <T> the type contained by the list.
	 * @param clazz the resource class to retrieve the list of.
	 * @param id the id of the resource to fetch. 
	 * @return the corresponding resource, or null if no corresponding set or id.
	 */
	public <T extends EngineResource> T getResource(Class<T> clazz, String id)
	{
		ResourceSet<T> list;
		if ((list = getResourceSet(clazz)) == null)
			return null;
		else
			return list.get(id);
	}

	/**
	 * Mapping of name-to-resource and id-to-resource.
	 * @param <R> an EngineResource type.
	 */
	public static class ResourceSet<R extends EngineResource> implements Iterable<R>
	{
		private static final List<Class<?>> NUMERIC_CLASSES = new ArrayList<Class<?>>(12) 
		{
			private static final long serialVersionUID = 1160496069763796739L;
			{
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
			}
		};
		
		/** This class. */
		private Class<R> listClass;
		
		/** Name-to-id mapping. */
		private HashMap<String, R> idMap;
		/** Tag to id mapping. */
		private HashDequeMap<String, R> tagHash;
		/** Object index. */
		private HashMap<String, TreeDequeMap<Index, R>> indexMap;
		
		/** Index names. */
		private HashMap<String, ValueGetter> indexNameList;

		/**
		 * Creates a new EngineResourceList.
		 * @param clazz the resource class.
		 */
		ResourceSet(Class<R> clazz)
		{
			this.listClass = clazz;
			idMap = new HashMap<>();
			tagHash = new HashDequeMap<>();
			indexMap = new HashMap<>();
			indexNameList = new HashMap<>();
			
			Profile<R> profile = Utils.getProfile(clazz);
			
			// indices
			for (Map.Entry<String, FieldInfo> fieldEntry : profile.getPublicFieldsByName().entrySet())
			{
				FieldInfo fieldInfo = fieldEntry.getValue();
				Field field = fieldInfo.getField();
				Indexed anno = field.getAnnotation(Indexed.class);
				if (anno == null)
					continue;
				
				String name = Utils.isEmpty(anno.value()) ? fieldEntry.getKey() : anno.value();
	
				if (!NUMERIC_CLASSES.contains(field.getType()))
					throw new EngineSetupException("On class "+clazz.getSimpleName()+", Indexed field \""+name+"\" must return a numeric type.");
				if (indexNameList.containsKey(name))
					throw new EngineSetupException("On class "+clazz.getSimpleName()+", Index \""+name+"\" is already declared.");
					
				indexNameList.put(name, new ValueGetter(field));
			}
			for (Map.Entry<String, MethodInfo> methodSignature : profile.getGetterMethodsByName().entrySet())
			{
				MethodInfo methodInfo = methodSignature.getValue();
				Method method = methodInfo.getMethod();
				Indexed anno = method.getAnnotation(Indexed.class);
				if (anno == null)
					continue;

				String name = Utils.isEmpty(anno.value()) ? methodSignature.getKey() : anno.value();
				
				if (!NUMERIC_CLASSES.contains(methodInfo.getType()))
					throw new EngineSetupException("On class "+clazz.getSimpleName()+", Indexed getter \""+name+"\" must return a numeric type.");
				if (indexNameList.containsKey(name))
					throw new EngineSetupException("On class "+clazz.getSimpleName()+", Index \""+name+"\" is already declared.");
				
				indexNameList.put(name, new ValueGetter(method));
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
				tagHash.add(s, resource);
	
			// add indexed fields.
			reIndex(resource);
			
		}
	
		// indexes a resource.
		private void reIndex(R resource)
		{
			for (Map.Entry<String, ValueGetter> pair : indexNameList.entrySet())
			{
				TreeDequeMap<Index, R> map;
				if ((map = indexMap.get(pair.getKey())) == null)
				{
					map = new TreeDequeMap<Index, R>();
					indexMap.put(pair.getKey(), map);
				}
				
				map.add(new Index(pair.getValue().get(resource)), resource);
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
			Queue<R> queue;
			if ((queue = tagHash.get(tagName)) == null)
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
			
			TreeDequeMap<Index, R> map;
			if ((map = indexMap.get(indexName)) == null)
				return 0;
			
			Deque<R> queue;
			if ((queue = map.get(index)) == null)
				return 0;
			
			int i = offset;
			for (R res : queue)
			{
				out[i++] = res;
				if (i >= out.length)
					return i - offset;
			}
			
			return i - offset;
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
		public int getBeforeIndexValue(String indexName, Number value, R[] out)
		{
			return getBeforeIndexValue(indexName, value, out, 0);
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
		public int getBeforeIndexValue(String indexName, Number value, R[] out, int offset)
		{
			Index index = new Index(value);
	
			TreeDequeMap<Index, R> map;
			if ((map = indexMap.get(indexName)) == null)
				return 0;
	
			int i = offset;
			for (Index idx : map.navigableKeySet())
			{
				if (idx.compareTo(index) > 0)
					return i - offset;
				
				for (R res : map.get(idx))
				{
					out[i++] = res;
					if (i >= out.length)
						return i - offset;
				}
			}
				
			return i - offset;
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
		public int getAfterIndexValue(String indexName, Number value, R[] out)
		{
			return getAfterIndexValue(indexName, value, out, 0);
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
		public int getAfterIndexValue(String indexName, Number value, R[] out, int offset)
		{
			Index index = new Index(value);
			
			TreeDequeMap<Index, R> map;
			if ((map = indexMap.get(indexName)) == null)
				return 0;
	
			int i = offset;
			for (Index idx : map.descendingKeySet())
			{
				if (idx.compareTo(index) < 0)
					return i - offset;
				
				for (R res : map.get(idx))
				{
					out[i++] = res;
					if (i >= out.length)
						return i - offset;
				}
			}
				
			return i - offset;
		}
	
		@Override
		public Iterator<R> iterator()
		{
			return idMap.values().iterator();
		}
	
		public int size()
		{
			return idMap.size();
		}
		
		public boolean isEmpty()
		{
			return idMap.isEmpty();
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
					return (Number)Utils.getFieldValue(instance, field);
				if (method != null)
					return (Number)Utils.invokeBlind(method, instance);
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
	}
}
