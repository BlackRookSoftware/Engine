package com.blackrook.engine.resources;

/**
 * Interface for Engine Resource Lists that have an interval-indexed part.
 * @author Matthew Tropiano
 */
public interface EngineResourceInterval<R extends EngineResource>
{

	/**
	 * Returns a set of objects that intersect with an indexed interval.
	 * @param indexName the name of the index.
	 * @param valueMin the minimum value to search for.
	 * @param valueMax the maximum value to search for.
	 * @param out the array to return the found values into (from the start of the array - stops at the end if more objects would be returned).
	 * @return the amount of objects returned.
	 */
	public int getByIntersect(String indexName, Object valueMin, Object valueMax, R[] out);
	
}
