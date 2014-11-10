/*******************************************************************************
 * Copyright (c) 2013-2014 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.struct;

import com.blackrook.commons.Common;
import com.blackrook.commons.hash.CaseInsensitiveHashMap;

/**
 * Used for storing and retrieving settings.
 * @author Matthew Tropiano
 */
public class ESettings extends CaseInsensitiveHashMap<Object>
{
	/**
	 * Returns a boolean value for a key.
	 * @param key the key for value retrieval.
	 * @return a parsed boolean value.
	 */
	public boolean getBoolean(String key)
	{
		Object obj = get(key);
		if (key == null)
			return false;
		else if (obj instanceof Boolean)
			return ((Boolean)obj);
		else if (obj instanceof Number)
			return ((Number)obj).doubleValue() != 0.0;
		else
			return Common.parseBoolean(String.valueOf(obj), false);
	}
	
	/**
	 * Returns an integer value for a key.
	 * @param key the key for value retrieval.
	 * @return a parsed integer value.
	 */
	public int getInteger(String key)
	{
		Object obj = get(key);
		if (key == null)
			return 0;
		else if (obj instanceof Boolean)
			return ((Boolean)obj) ? 1 : 0;
		else if (obj instanceof Number)
			return ((Number)obj).intValue();
		else
			return Common.parseInt(String.valueOf(obj), 0);
	}
	
	/**
	 * Returns a double value for a key.
	 * @param key the key for value retrieval.
	 * @return a parsed double value.
	 */
	public double getDouble(String key)
	{
		Object obj = get(key);
		if (key == null)
			return 0.0;
		else if (obj instanceof Boolean)
			return ((Boolean)obj) ? 1.0 : 0.0;
		else if (obj instanceof Number)
			return ((Number)obj).doubleValue();
		else
			return Common.parseDouble(String.valueOf(obj), 0.0);
	}
	
	/**
	 * Returns a string value for a key.
	 * @param key the key for value retrieval.
	 * @return a string representation of a value.
	 */
	public String getString(String key)
	{
		Object obj = get(key);
		if (obj == null)
			return null;
		else if (obj instanceof String)
			return (String)obj;
		else
			return String.valueOf(obj);
	}
	
}
