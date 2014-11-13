package com.blackrook.engine.resources;

/**
 * Interface for Engine Resource Lists that have an indexed part.
 * @author Matthew Tropiano
 */
public interface EngineResourceIndexed<R extends EngineResource>
{

	/**
	 * Returns a set of objects that are correspond to an index value and before.
	 * @param indexName the name of the index.
	 * @param value the value to search for.
	 * @param equal if true, get before and EQUALS.
	 * @param out the array to return the found values into (from the start of the array - stops at the end if more objects would be returned).
	 * @return the amount of objects returned.
	 */
	public int getBeforeIndex(String indexName, Object value, boolean equal, R[] out);
	
	/**
	 * Returns a set of objects that are correspond to an index value.
	 * @param indexName the name of the index.
	 * @param value the value to search for.
	 * @param out the array to return the found values into (from the start of the array - stops at the end if more objects would be returned).
	 * @return the amount of objects returned.
	 */
	public int getByIndex(String indexName, Object value, R[] out);
	
	/**
	 * Returns a set of objects that are correspond to an index value and after.
	 * @param indexName the name of the index.
	 * @param value the value to search for.
	 * @param equal if true, get after and EQUALS.
	 * @param out the array to return the found values into (from the start of the array - stops at the end if more objects would be returned).
	 * @return the amount of objects returned.
	 */
	public int getAfterIndex(String indexName, Object value, boolean equal, R[] out);
	
}
