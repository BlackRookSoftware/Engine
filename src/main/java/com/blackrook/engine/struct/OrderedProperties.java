/*******************************************************************************
 * Copyright (c) 2016-2021 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
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
