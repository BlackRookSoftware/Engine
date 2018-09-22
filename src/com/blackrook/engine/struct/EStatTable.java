/*******************************************************************************
 * Copyright (c) 2016-2018 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.struct;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;

import com.blackrook.commons.Common;
import com.blackrook.commons.hash.HashMap;
import com.blackrook.io.SuperReader;
import com.blackrook.io.SuperWriter;

/**
 * A table that stores stat data and can 
 * be saved as a blob of data with checksum.
 * @author Matthew Tropiano
 */
public final class EStatTable extends HashMap<Integer, Double>
{
	/**
	 * Creates the stat table.
	 */
	public EStatTable()
	{
	}
	
	/**
	 * Gets a stat as a double.
	 * @param type the requested type.
	 * @return the double value.
	 */
	public double getDouble(int type)
	{
		return get(type);
	}
	
	/**
	 * Gets a stat as a float.
	 * @param type the requested type.
	 * @return the float value.
	 */
	public float getFloat(int type)
	{
		return (float)getDouble(type);
	}

	/**
	 * Gets a stat as a long integer.
	 * @param type the requested type.
	 * @return the long value.
	 */
	public long getLong(int type)
	{
		return (long)getDouble(type);
	}

	/**
	 * Gets a stat as a integer.
	 * @param type the requested type.
	 * @return the integer value.
	 */
	public int getInteger(int type)
	{
		return (int)getDouble(type);
	}

	/**
	 * Gets a stat as a boolean (nonzero is true).
	 * @param type the requested type.
	 * @return the boolean value.
	 */
	public boolean getBoolean(int type)
	{
		return getDouble(type) != 0.0;
	}

	/**
	 * Gets a stat as a double.
	 * @param type the requested type (uses ordinal).
	 * @return the double value.
	 */
	public double getDouble(Enum<?> type)
	{
		return get(type.ordinal());
	}

	/**
	 * Gets a stat as a float.
	 * @param type the requested type (uses ordinal).
	 * @return the float value.
	 */
	public float getFloat(Enum<?> type)
	{
		return (float)getDouble(type.ordinal());
	}

	/**
	 * Gets a stat as a long integer.
	 * @param type the requested type (uses ordinal).
	 * @return the long value.
	 */
	public long getLong(Enum<?> type)
	{
		return (long)getDouble(type.ordinal());
	}

	/**
	 * Gets a stat as a integer.
	 * @param type the requested type (uses ordinal).
	 * @return the integer value.
	 */
	public int getInteger(Enum<?> type)
	{
		return (int)getDouble(type.ordinal());
	}

	/**
	 * Gets a stat as a boolean (nonzero is true).
	 * @param type the requested type (uses ordinal).
	 * @return the boolean value.
	 */
	public boolean getBoolean(Enum<?> type)
	{
		return getDouble(type.ordinal()) != 0.0;
	}

	/**
	 * Sets a stat value.
	 * @param type the requested type.
	 * @param value the value to set.
	 */
	public void set(int type, double value)
	{
		put(type, value);
	}
	
	/**
	 * Sets a stat value.
	 * @param type the requested type.
	 * @param value the value to set.
	 */
	public void set(Enum<?> type, double value)
	{
		set(type.ordinal(), value);
	}
	
	/**
	 * Sets a stat value.
	 * @param type the requested type.
	 * @param value the value to set.
	 */
	public void set(int type, float value)
	{
		put(type, (double)value);
	}
	
	/**
	 * Sets a stat value.
	 * @param type the requested type.
	 * @param value the value to set.
	 */
	public void set(Enum<?> type, float value)
	{
		set(type.ordinal(), value);
	}
	
	/**
	 * Sets a stat value.
	 * @param type the requested type.
	 * @param value the value to set.
	 */
	public void set(int type, long value)
	{
		put(type, (double)value);
	}
	
	/**
	 * Sets a stat value.
	 * @param type the requested type.
	 * @param value the value to set.
	 */
	public void set(Enum<?> type, long value)
	{
		set(type.ordinal(), value);
	}
	
	/**
	 * Sets a stat value.
	 * @param type the requested type.
	 * @param value the value to set.
	 */
	public void set(int type, int value)
	{
		put(type, (double)value);
	}
	
	/**
	 * Sets a stat value.
	 * @param type the requested type.
	 * @param value the value to set.
	 */
	public void set(Enum<?> type, int value)
	{
		set(type.ordinal(), value);
	}
	
	/**
	 * Sets a stat value (true is 1, false is 0).
	 * @param type the requested type.
	 * @param value the value to set.
	 */
	public void set(int type, boolean value)
	{
		put(type, value ? 1.0 : 0.0);
	}
	
	/**
	 * Sets a stat value (true is 1, false is 0).
	 * @param type the requested type.
	 * @param value the value to set.
	 */
	public void set(Enum<?> type, boolean value)
	{
		set(type.ordinal(), value);
	}
	
	/**
	 * Adds a table to this.
	 * @param table the table to add to this one.
	 */
	public void add(EStatTable table)
	{
		Iterator<Integer> it = table.keyIterator();
		while (it.hasNext())
		{
			int type = it.next();
			set(type, getDouble(type) + table.getDouble(type));
		}
	}
	
	/**
	 * Read state.
	 * @param in the input stream.
	 * @throws IOException if a read error occurred.
	 */
	public void readState(InputStream in) throws IOException
	{
		clear();
		SuperReader sr = new SuperReader(in, SuperReader.LITTLE_ENDIAN);
		
		byte[] data = sr.readByteArray();
		byte[] hash = sr.readBytes(20);

		if (!Arrays.equals(Common.sha1(data), hash))
			throw new IOException("Hash does not match data. Table may be corrupt.");
		
		SuperReader datasr = new SuperReader(new ByteArrayInputStream(data), SuperReader.LITTLE_ENDIAN);
		int n = datasr.readInt();
		
		for (int i = 0; i < n; i++)
		{
			int type = datasr.readInt();
			double value = datasr.readDouble();
			set(type, value);
		}
	}
	
	/**
	 * Write state.
	 * @param out the output stream.
	 * @throws IOException if a write error occurred.
	 */
	public void writeState(OutputStream out) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		SuperWriter sw = new SuperWriter(bos, SuperWriter.LITTLE_ENDIAN);
		
		sw.writeInt(size());
		
		Iterator<Integer> it = keyIterator();
		while (it.hasNext())
		{
			int type = it.next();
			sw.writeInt(type);
			sw.writeDouble(getDouble(type));
		}
		
		byte[] b = bos.toByteArray();
		byte[] sha1 = Common.sha1(b);
		
		SuperWriter swout = new SuperWriter(out, SuperWriter.LITTLE_ENDIAN);
		swout.writeByteArray(b);
		swout.writeBytes(sha1);
	}
	
}
