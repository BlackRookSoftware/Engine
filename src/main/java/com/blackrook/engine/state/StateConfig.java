package com.blackrook.engine.state;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * A state configuration object.
 * Holds parameters for a state transition and used on state entry.
 * @author Matthew Tropiano
 */
public class StateConfig
{
	/** Mapping of parameter name to object. */
	private Map<String, Object> parameterMap;
	
	/**
	 * Protected constructor for config.
	 */
	protected StateConfig()
	{
		this.parameterMap = new HashMap<>();
	}
	
	/**
	 * Sets a config parameter.
	 * @param name the parameter name.
	 * @param value the parameter value.
	 */
	public void setParameter(String name, Object value)
	{
		parameterMap.put(name, value);
	}
	
	/**
	 * Gets a config parameter, casting it to the desired type.
	 * @param <T> the type casted to.
	 * @param type the class type to cast to.
	 * @param name the parameter name.
	 * @return the corresponding value, or null if not found.
	 * @throws ClassCastException if the value could not be casted to the desired type.
	 */
	public <T> T getParameter(Class<T> type, String name)
	{
		return type.cast(parameterMap.get(name));
	}
	
	/**
	 * Creates a single mapping entry.
	 * @param <T> the value type.
	 * @param name the parameter name.
	 * @param value the parameter value.
	 * @return a new entry.
	 * @see #createConfig(java.util.Map.Entry...)
	 */
	public static <T> Map.Entry<String, T> entry(String name, T value)
	{
		return new AbstractMap.SimpleEntry<>(name, value);
	}
	
	/**
	 * Creates a new state config using entries.
	 * @param entries the parameter mapping entries.
	 * @return a new state config.
	 * @see #entry(String, Object)
	 */
	@SafeVarargs
	public static StateConfig createConfig(Map.Entry<String, Object> ... entries)
	{
		StateConfig out = new StateConfig();
		for (int i = 0; i < entries.length; i++)
			out.setParameter(entries[i].getKey(), entries[i].getValue());
		return out;
	}
	
}
