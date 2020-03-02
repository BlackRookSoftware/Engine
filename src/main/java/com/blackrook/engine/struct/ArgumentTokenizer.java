/*******************************************************************************
 * Copyright (c) 2016-2020 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.struct;

/**
 * String splitting class that parses strings by 
 * whitespace and quote wrapping (single, double, or "backtick") and returns them as different formats.
 * Not threadsafe.
 * @author Matthew Tropiano
 */
public class ArgumentTokenizer
{
	/** The character sequence to read from. */
	private CharSequence sequence;
	
	/** Current index. */
	private int currentIndex;
	
	/**
	 * Creates an argument tokenizer.
	 * @param sequence the character sequence to parse.
	 */
	public ArgumentTokenizer(CharSequence sequence)
	{
		this.sequence = sequence;
		this.currentIndex = 0;
	}
	
	private int readChar()
	{
		if (currentIndex >= sequence.length())
			return -1; 
		return sequence.charAt(currentIndex++);
	}
	
	/**
	 * @return the next token, or <code>null</code> if no more tokens could be parsed.
	 */
	public String nextToken()
	{
		final int STATE_INIT = 0;
		final int STATE_TOKEN = 1;
		final int STATE_DQUOTEDTOKEN = 2;
		final int STATE_SQUOTEDTOKEN = 3;
		final int STATE_BQUOTEDTOKEN = 4;
		int state = STATE_INIT;
		StringBuilder sb = new StringBuilder(64);
		
		int n;
		while ((n = readChar()) >= 0)
		{
			char c = (char)n;
			switch (state)
			{
				case STATE_INIT:
				{
					if (c == '"')
						state = STATE_DQUOTEDTOKEN;
					else if (c == '\'')
						state = STATE_SQUOTEDTOKEN;
					else if (c == '`')
						state = STATE_BQUOTEDTOKEN;
					else if (Character.isWhitespace(c))
						state = STATE_INIT;
					else
					{
						sb.append(c);
						state = STATE_TOKEN;
					}	
				}
				break;
				
				case STATE_TOKEN:
				{
					if (Character.isWhitespace(c))
						return sb.toString();
					else
						sb.append(c);
				}
				break;
				
				case STATE_DQUOTEDTOKEN:
				{
					if (c == '"')
						return sb.toString();
					else
						sb.append(c);
				}
				break;
				
				case STATE_SQUOTEDTOKEN:
				{
					if (c == '\'')
						return sb.toString();
					else
						sb.append(c);
				}
				break;
				
				case STATE_BQUOTEDTOKEN:
				{
					if (c == '`')
						return sb.toString();
					else
						sb.append(c);
				}
				break;
			}
		}
		return null;
	}

	/**
	 * Reads the next token and parses it as a boolean value.
	 * @return the next boolean value, or <code>false</code> if the token is not parseable or <code>null</code>.
	 */
	public boolean nextBoolean()
	{
		return parseBoolean(nextToken(), false);
	}
	
	/**
	 * Reads the next token and parses it as a boolean value.
	 * @param blank the value to return if the value is not parseable or if at the end of the tokenizer.
	 * @return the next boolean value, or <code>false</code> if the token is not parseable or <code>blank</code> if <code>null</code>.
	 */
	public boolean nextBoolean(boolean blank)
	{
		return parseBoolean(nextToken(), blank);
	}
	
	/**
	 * Reads the next token and parses it as a byte value.
	 * @return the next byte value, or <code>0</code> if the token is not parseable or <code>null</code>.
	 */
	public byte nextByte()
	{
		return parseByte(nextToken(), (byte)0);
	}
	
	/**
	 * Reads the next token and parses it as a byte value.
	 * @param blank the value to return if the value is not parseable or if at the end of the tokenizer.
	 * @return the next byte value, or <code>0</code> if the token is not parseable or <code>blank</code> if <code>null</code>.
	 */
	public byte nextByte(byte blank)
	{
		return parseByte(nextToken(), blank);
	}
	
	/**
	 * Reads the next token and parses it as a short value.
	 * @return the next short value, or <code>0</code> if the token is not parseable or <code>null</code>.
	 */
	public short nextShort()
	{
		return parseShort(nextToken(), (short)0);
	}
	
	/**
	 * Reads the next token and parses it as a short value.
	 * @param blank the value to return if the value is not parseable or if at the end of the tokenizer.
	 * @return the next short value, or <code>0</code> if the token is not parseable or <code>blank</code> if <code>null</code>.
	 */
	public short nextShort(short blank)
	{
		return parseShort(nextToken(), blank);
	}
	
	/**
	 * Reads the next token and parses it as a char value.
	 * @return the next char value, or <code>'\0'</code> if the token is not parseable or <code>null</code>.
	 */
	public char nextChar()
	{
		return parseChar(nextToken(), '\0');
	}
	
	/**
	 * Reads the next token and parses it as a char value.
	 * @param blank the value to return if the value is not parseable or if at the end of the tokenizer.
	 * @return the next char value, or <code>0</code> if the token is not parseable or <code>blank</code> if <code>null</code>.
	 */
	public char nextChar(char blank)
	{
		return parseChar(nextToken(), blank);
	}
	
	/**
	 * Reads the next token and parses it as an integer value.
	 * @return the next integer value, or <code>0</code> if the token is not parseable or <code>null</code>.
	 */
	public int nextInt()
	{
		return parseInt(nextToken(), 0);
	}
	
	/**
	 * Reads the next token and parses it as an integer value.
	 * @param blank the value to return if the value is not parseable or if at the end of the tokenizer.
	 * @return the next integer value, or <code>0</code> if the token is not parseable or <code>blank</code> if <code>null</code>.
	 */
	public int nextInt(int blank)
	{
		return parseInt(nextToken(), blank);
	}
	
	/**
	 * Reads the next token and parses it as a long value.
	 * @return the next long value, or <code>0</code> if the token is not parseable or <code>null</code>.
	 */
	public long nextLong()
	{
		return parseLong(nextToken(), 0L);
	}
	
	/**
	 * Reads the next token and parses it as a long value.
	 * @param blank the value to return if the value is not parseable or if at the end of the tokenizer.
	 * @return the next long value, or <code>blank</code> if the token is not parseable or <code>blank</code> if <code>null</code>.
	 */
	public long nextLong(long blank)
	{
		return parseLong(nextToken(), blank);
	}
	
	/**
	 * Reads the next token and parses it as a float value.
	 * @return the next float value, or <code>0f</code> if the token is not parseable or <code>null</code>.
	 */
	public float nextFloat()
	{
		return parseFloat(nextToken(), 0f);
	}
	
	/**
	 * Reads the next token and parses it as a float value.
	 * @param blank the value to return if the value is not parseable or if at the end of the tokenizer.
	 * @return the next float value, or <code>0f</code> if the token is not parseable or <code>blank</code> if <code>null</code>.
	 */
	public float nextFloat(float blank)
	{
		return parseFloat(nextToken(), blank);
	}
	
	/**
	 * Reads the next token and parses it as a double value.
	 * @return the next double value, or <code>0.0</code> if the token is not parseable or <code>null</code>.
	 */
	public double nextDouble()
	{
		return parseDouble(nextToken(), 0.0);
	}
	
	/**
	 * Reads the next token and parses it as a double value.
	 * @param blank the value to return if the value is not parseable or if at the end of the tokenizer.
	 * @return the next double value, or <code>0.0</code> if the token is not parseable or <code>blank</code> if <code>null</code>.
	 */
	public double nextDouble(double blank)
	{
		return parseDouble(nextToken(), blank);
	}
	
	private static boolean isStringEmpty(Object obj)
	{
		if (obj == null)
			return true;
		else
			return ((String)obj).trim().length() == 0;
	}

	/**
	 * Attempts to parse a boolean from a string.
	 * If the string is null or the empty string, this returns <code>def</code>.
	 * If the string does not equal "true," this returns false.
	 * @param s the input string.
	 * @param def the fallback value to return.
	 * @return the interpreted boolean or def if the input string is blank.
	 */
	private static boolean parseBoolean(String s, boolean def)
	{
		if (isStringEmpty(s))
			return def;
		else if (!s.equalsIgnoreCase("true"))
			return false;
		else
			return true;
	}

	/**
	 * Attempts to parse a byte from a string.
	 * If the string is null or the empty string, this returns <code>def</code>.
	 * @param s the input string.
	 * @param def the fallback value to return.
	 * @return the interpreted byte or def if the input string is blank.
	 */
	private static byte parseByte(String s, byte def)
	{
		if (isStringEmpty(s))
			return def;
		try {
			return Byte.parseByte(s);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * Attempts to parse a short from a string.
	 * If the string is null or the empty string, this returns <code>def</code>.
	 * @param s the input string.
	 * @param def the fallback value to return.
	 * @return the interpreted short or def if the input string is blank.
	 */
	private static short parseShort(String s, short def)
	{
		if (isStringEmpty(s))
			return def;
		try {
			return Short.parseShort(s);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * Attempts to parse a byte from a string.
	 * If the string is null or the empty string, this returns <code>def</code>.
	 * @param s the input string.
	 * @param def the fallback value to return.
	 * @return the first character in the string or def if the input string is blank.
	 */
	private static char parseChar(String s, char def)
	{
		if (isStringEmpty(s))
			return def;
		else
			return s.charAt(0);
	}

	/**
	 * Attempts to parse an int from a string.
	 * If the string is null or the empty string, this returns <code>def</code>.
	 * @param s the input string.
	 * @param def the fallback value to return.
	 * @return the interpreted integer or def if the input string is blank.
	 */
	private static int parseInt(String s, int def)
	{
		if (isStringEmpty(s))
			return def;
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * Attempts to parse a long from a string.
	 * If the string is null or the empty string, this returns <code>def</code>.
	 * @param s the input string.
	 * @param def the fallback value to return.
	 * @return the interpreted long integer or def if the input string is blank.
	 */
	private static long parseLong(String s, long def)
	{
		if (isStringEmpty(s))
			return def;
		try {
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
			return 0L;
		}
	}

	/**
	 * Attempts to parse a float from a string.
	 * If the string is null or the empty string, this returns <code>def</code>.
	 * @param s the input string.
	 * @param def the fallback value to return.
	 * @return the interpreted float or def if the input string is blank.
	 */
	private static float parseFloat(String s, float def)
	{
		if (isStringEmpty(s))
			return def;
		try {
			return Float.parseFloat(s);
		} catch (NumberFormatException e) {
			return 0f;
		}
	}

	/**
	 * Attempts to parse a double from a string.
	 * If the string is null or the empty string, this returns <code>def</code>.
	 * @param s the input string.
	 * @param def the fallback value to return.
	 * @return the interpreted double or def if the input string is blank.
	 */
	private static double parseDouble(String s, double def)
	{
		if (isStringEmpty(s))
			return def;
		try {
			return Double.parseDouble(s);
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}
	
	
}
