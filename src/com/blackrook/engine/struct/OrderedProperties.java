package com.blackrook.engine.struct;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

/**
 * A special properties type that returns keys in lexicographical order. 
 * @author Matthew Tropiano
 */
public class OrderedProperties extends Properties
{
	private static final long serialVersionUID = -2948000047148212090L;

	@Override
	public synchronized Enumeration<Object> keys()
	{
        return Collections.enumeration(new TreeSet<Object>(super.keySet()));
    }
	
}
